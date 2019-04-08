package com.lru.example;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;

public class RestVerticle extends AbstractVerticle {
    private final static Logger LOG = LoggerFactory.getLogger(RestVerticle.class);

    private final HttpServer httpServer;
    private final static int PORT = 8888;
    private final static String VALUE_STRING = "value=";
    private final LRUCache cache;

    public RestVerticle() {
        cache = new LRUCache(5); // size is assumed by default, can be modified later

        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);
        router.exceptionHandler(this::handleException);

        router.route().handler(BodyHandler.create());
        router.put("/api/v1/put/:key").handler(this::putValue);
        router.get("/api/v1/get/:key").handler(this::getValue);
        this.httpServer = vertx.createHttpServer();
        this.httpServer.requestHandler(router::accept);
        this.httpServer.listen(PORT, event -> {
            if (event.succeeded())
                LOG.info("Successfully started HTTP server on port " + PORT);
            else if (event.failed())
                LOG.info("Exception starting HTTP server on port " + PORT);
        });
    }

    private void putValue(RoutingContext ctx) {
        final HttpServerResponse response = ctx.response();
        int returnCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
        JsonObject returnMessage = new JsonObject();
        try {
            String key = ctx.request().getParam("key");
            String data = ctx.getBodyAsString();
            if (key != null && !key.isEmpty() && isValidData(data)) {
                int keyInt = Integer.parseInt(key);
                int value = Integer.parseInt(data.replaceAll(VALUE_STRING, ""));
                Optional<Node> evictedValue = cache.put(keyInt, value);
                cache.printCurrentStack();
                if (evictedValue.isPresent()) {
                    returnMessage.put("key", evictedValue.get().getKey());
                    returnMessage.put("value", evictedValue.get().getValue());
                }
                returnCode = HttpResponseStatus.OK.code();
            } else
                returnMessage = new JsonObject("{\"Exception\": \"Empty/Null key provided\"}");
        } catch (Exception e) {
            LOG.error("Exception : " + e.getMessage());
            e.printStackTrace();
            returnMessage = new JsonObject("{\"Exception\": \"" + e.getMessage() + "\"}");
        } finally {
            returnJsonResponse(response, returnMessage, returnCode);
        }
    }

    private void getValue(RoutingContext ctx) {
        
    }

    private boolean isValidData(String data) {
        return (data != null && !data.isEmpty() && data.startsWith(VALUE_STRING));
    }

    private void returnJsonResponse(HttpServerResponse response, JsonObject jsonMessage, int code) {
        final String stringMessage = jsonMessage.encodePrettily();
        response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        response.putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(stringMessage.length()));
        response.setStatusCode(code);
        response.end(stringMessage);
    }

    private void handleException(Throwable throwable) {
        LOG.error("Exception in Handling API : " + throwable.getMessage());
        throwable.printStackTrace();
    }

    public static void main(String[] args) {
        new RestVerticle();
    }
}
