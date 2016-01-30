package kry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class ServiceCheck extends AbstractVerticle {

  @Override
  public void start(Future<Void> fut) {
    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);

    router.route(HttpMethod.GET, "/service").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.end("GET");
    });

    router.route(HttpMethod.POST, "/service").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      HttpServerRequest request = routingContext.request();
      response.end("POST");

    });
    router.route(HttpMethod.DELETE, "/service/*").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      HttpServerRequest request = routingContext.request();
      response.end("DELETE");
    });

    //setup static stuff, will serve from webroot...
    router.route().handler(StaticHandler.create());
    server.requestHandler(router::accept).listen(8080, result -> {
          if (result.succeeded()) {
            fut.complete();
          } else {
            fut.fail(result.cause());
          }
        });
  }
}
