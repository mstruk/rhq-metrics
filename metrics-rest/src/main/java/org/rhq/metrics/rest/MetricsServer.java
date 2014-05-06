package org.rhq.metrics.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

import org.rhq.metrics.core.DataAccess;
import org.rhq.metrics.core.DataType;
import org.rhq.metrics.core.MetricsService;
import org.rhq.metrics.core.RawNumericMetric;
import org.rhq.metrics.core.SchemaManager;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author John Sanda
 */
public class MetricsServer extends Verticle {

    public static final String NODE_ADDRESSES = "nodes";

    public static final String KEYSPACE = "cluster";

    public static final String CQL_PORT = "cqlPort";

    public static final String HTTP_PORT = "httpPort";

    public static final String LOG4J_CONF_FILE = "log4jConfFile";

    public static final Logger logger = LoggerFactory.getLogger(MetricsServer.class);

    @Override
    public void start() {
        String log4jConfFile = container.config().getString(LOG4J_CONF_FILE);
        LogManager.resetConfiguration();
        if (log4jConfFile == null) {
            PropertyConfigurator.configure(getClass().getResourceAsStream("/default.log4j.properties"));
        } else {
            PropertyConfigurator.configure(log4jConfFile);
        }

        logger.info("Starting metrics server...");

        Cluster cluster = new Cluster.Builder()
            .addContactPoints(getContactPoints())
            .withPort(container.config().getNumber(CQL_PORT, 9042).intValue())
            .build();

        updateSchemaIfNecessary(cluster);

        Session session = cluster.connect(container.config().getString(KEYSPACE, "rhq"));

        final DataAccess dataAccess = new DataAccess(session);

        final MetricsService metricsService = new MetricsService();
        metricsService.setDataAccess(dataAccess);

        final ObjectMapper mapper = new ObjectMapper();

        RouteMatcher routeMatcher = new RouteMatcher();
        routeMatcher.get("/rhq-metrics/:id/data", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest request) {
                final String id = request.params().get("id");

                long start;
                String startParam = request.params().get("start");
                if (startParam == null) {
                    start = DateTime.now().minusHours(8).getMillis();
                } else {
                    start = Long.parseLong(startParam);
                }

                long end;
                String endParam = request.params().get("end");
                if (endParam == null) {
                    end = DateTime.now().getMillis();
                } else {
                    end = Long.parseLong(endParam);
                }

                ResultSetFuture future = metricsService.findData("raw", id, start, end);

                Futures.addCallback(future, new FutureCallback<ResultSet>() {
                    public void onSuccess(ResultSet resultSet) {
                        JsonObject result = new JsonObject();
                        result.putString("bucket", "raw");
                        result.putString("id", id);
                        JsonArray data = new JsonArray();

                        for (Row row : resultSet) {
                            Map<Integer, Double> map = row.getMap(2, Integer.class, Double.class);
                            JsonObject jsonRow = new JsonObject();
                            jsonRow.putNumber("time", row.getDate(1).getTime());
                            jsonRow.putNumber("value", map.get(DataType.RAW.ordinal()));
                            data.addObject(jsonRow);
                        }
                        result.putArray("data", data);

                        request.response().end(result.toString());
                    }

                    public void onFailure(Throwable t) {
                        request.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                        request.response().setStatusMessage("Failed to retrieve data: " + t.getMessage());
                        request.response().end();
                    }
                });
            }
        });

        routeMatcher.post("/rhq-metrics/:id/data", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    public void handle(Buffer body) {
                        try {
                            RawData rawData = mapper.readValue(body.getBytes(), RawData.class);

                            metricsService.addData(ImmutableSet.of(new RawNumericMetric(rawData.id, rawData.value,
                                rawData.timestamp)));

                            request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                            request.response().end();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        routeMatcher.get("/rhq-metrics/data", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest request) {
                final List<String> ids = request.params().getAll("id");
                if (ids.isEmpty()) {
                    request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                    request.response().end();
                } else {
                    long start;
                    String startParam = request.params().get("start");
                    if (startParam == null) {
                        start = DateTime.now().minusHours(8).getMillis();
                    } else {
                        start = Long.parseLong(startParam);
                    }

                    long end;
                    String endParam = request.params().get("end");
                    if (endParam == null) {
                        end = DateTime.now().getMillis();
                    } else {
                        end = Long.parseLong(endParam);
                    }
                    final AtomicInteger count = new AtomicInteger();
                    final Pump pump = Pump.createPump(request, request.response()).start();

                    request.response().setChunked(true);

                    for (final String id : ids) {
                        JsonObject message = new JsonObject()
                            .putString("id", id)
                            .putNumber("start", start)
                            .putNumber("end", end);
                        vertx.eventBus().send("find-metrics", message, new Handler<Message<JsonObject>>() {
                            public void handle(Message<JsonObject> event) {
                                JsonObject result = event.body();
                                request.response().write(result.toString());
                                if (count.addAndGet(1) == ids.size()) {
                                    pump.stop();
                                    request.response().setStatusCode(HttpResponseStatus.OK.code());
                                    request.response().end();
                                }
                            }
                        });
                    }
                }
            }
        });

        routeMatcher.post("/rhq-metrics/data", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    public void handle(Buffer body) {
                        try {
                            List<RawData> rawData = mapper.readValue(body.getBytes(),
                                TypeFactory.defaultInstance().constructCollectionType(List.class, RawData.class));
                            Set<RawNumericMetric> rawMetrics = new HashSet<RawNumericMetric>();

                            for (RawData datum : rawData) {
                                rawMetrics.add(new RawNumericMetric(datum.id, datum.value, datum.timestamp));
                            }

                            metricsService.addData(rawMetrics);

                            request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                             request.response().end();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        vertx.eventBus().registerHandler("find-metrics", new Handler<Message<JsonObject>>() {
            public void handle(final Message<JsonObject> event) {
                JsonObject request = event.body();
                final String id = request.getString("id");
                long start = request.getNumber("start").longValue();
                long end = request.getNumber("end").longValue();

                ResultSetFuture future = dataAccess.findData("raw", id, start, end);
                Futures.addCallback(future, new FutureCallback<ResultSet>() {
                    public void onSuccess(ResultSet resultSet) {
                        JsonObject result = new JsonObject();
                        result.putString("bucket", "raw");
                        result.putString("id", id);
                        JsonArray data = new JsonArray();

                        for (Row row : resultSet) {
                            Map<Integer, Double> map = row.getMap(2, Integer.class, Double.class);
                            JsonObject jsonRow = new JsonObject();
                            jsonRow.putNumber("time", row.getDate(1).getTime());
                            jsonRow.putNumber("value", map.get(DataType.RAW.ordinal()));
                            data.addObject(jsonRow);
                        }
                        result.putArray("data", data);

                        event.reply(result);
                    }

                    public void onFailure(Throwable t) {

                    }
                });
            }
        });

        vertx.createHttpServer().requestHandler(routeMatcher).listen(container.config().getNumber(HTTP_PORT,
            7474).intValue());
    }

    private void updateSchemaIfNecessary(Cluster cluster) {
        try (Session session = cluster.connect("system")) {
            SchemaManager schemaManager = new SchemaManager(session);
            schemaManager.updateSchema();
        }
    }

    private String[] getContactPoints() {
        JsonArray addresses = container.config().getArray(NODE_ADDRESSES);
        if (addresses == null) {
            return new String[] {"127.0.0.1"};
        } else {
            String[] contactPoints = new String[addresses.size()];
            for (int i = 0; i < addresses.size(); ++i) {
                contactPoints[i] = addresses.get(i);
            }
            return contactPoints;
        }
    }

}
