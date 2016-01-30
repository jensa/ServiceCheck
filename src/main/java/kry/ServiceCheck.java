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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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
        response.setStatusCode(500);
        response.end("{services:[]}");
      }
    });

    router.route(HttpMethod.POST, "/service").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      HttpServerRequest request = routingContext.request();
      request.bodyHandler(body -> {
        try{
          System.out.println(body.toString());
          JsonObject newService = new JsonObject(body.toString());
          newService.put("id", java.util.UUID.randomUUID().toString());
          //consider checking status immediately, we probably should
          JsonObject json = new JsonObject(readDbContents());
          JsonArray services = json.getJsonArray("services");
          services.add(newService);
          json.put("services", services);
          Files.write(Paths.get(dbFile), json.toString().getBytes());
          response.putHeader("content-type", "text/json; charset=utf-8");
          // Write to the response and end it
          response.end(newService.toString());
        } catch(Exception e){
          response.setStatusCode(500);
          response.end("Failed to create new service:\\n" + e.toString());
        }
      });
    });

    router.route(HttpMethod.DELETE, "/service/*").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      HttpServerRequest request = routingContext.request();

      //id is last part of request URL
      String uri = request.uri();
      String id = uri.substring(uri.lastIndexOf("/")+1);
      try{
        JsonObject json = new JsonObject(readDbContents());
        JsonArray services = json.getJsonArray("services");
        for(int i = 0;i < services.size();i++){
          JsonObject service = services.getJsonObject(i);
          if(service.getString("id").equals(id)){
            services.remove(i);
            break;
          }
        }
        json.put("services", services);
        Files.write(Paths.get(dbFile), json.toString().getBytes());
        // Write to the response and end it
        response.putHeader("content-type", "text/json; charset=utf-8");
        response.end("{}");
      } catch(Exception e){
        response.setStatusCode(500);
        response.end("Error deleting service:\\n"+e.toString());
      }
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
