package kry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;

public class ServiceCheck extends AbstractVerticle {

  private String dbFile = "dbfile.json";

  private String readDbContents() throws IOException{
    File f = new File(dbFile);
    if(f.isDirectory())
      return "";
    if(!f.exists())
      f.createNewFile();
    return new String(Files.readAllBytes(Paths.get(dbFile)));
  }

  @Override
  public void start(Future<Void> fut) {
    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);

    router.route(HttpMethod.GET, "/service").handler(routingContext -> {
        //return all services
      HttpServerResponse response = routingContext.response();
      try{
        response.putHeader("content-type", "text/json; charset=utf-8");
        //read from file and just write all of it:
        String dbContent = readDbContents();
        response.end(dbContent);
      } catch(Exception e){
        //return an object with an empty array
        response.end("{services:[]}");
      }
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
