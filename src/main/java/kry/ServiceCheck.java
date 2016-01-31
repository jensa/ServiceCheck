package kry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ServiceCheck extends AbstractVerticle {

  private ServiceCheckBackgroundService backgroundService;
  @Override
  public void start(Future<Void> fut) {
    HttpServer server = vertx.createHttpServer();

    backgroundService = new ServiceCheckBackgroundService(vertx);
    if(config().getBoolean("runBackgroundService")){
      //start background service immediately
      backgroundService.Run();

    }

    Router router = Router.router(vertx);

    router.route(HttpMethod.GET, "/service").handler(routingContext -> {
        //return all services
      HttpServerResponse response = routingContext.response();
      try{
        response.putHeader("content-type", "text/json; charset=utf-8");
        //read from file and just write all of it:
        String dbContent = Db.readDbContents();
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
          JsonObject jsonObject = new JsonObject(body.toString());
          jsonObject.put("id", java.util.UUID.randomUUID().toString());
          String url = jsonObject.getString("url");
          if(!(url.startsWith("http://")))
            url = "http://" + url;
          jsonObject.put("url", url);
          backgroundService.checkOne(jsonObject, newService ->
          {
            try{
              JsonObject dbContents = new JsonObject(Db.readDbContents());
              JsonArray services = dbContents.getJsonArray("services");
              services.add(newService);

              dbContents.put("services", services);
              Db.writeToDb(dbContents);
              response.putHeader("content-type", "text/json; charset=utf-8");
              // Write to the response and end it
              response.end(newService.toString());
            } catch(Exception e){
              response.setStatusCode(500);
              response.end("Failed to create new service:\\n" + e.toString());
            }
          });
      });
    });

    router.route(HttpMethod.DELETE, "/service/*").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      HttpServerRequest request = routingContext.request();

      //id is last part of request URL
      String uri = routingContext.normalisedPath();
      String id = uri.substring(uri.lastIndexOf("/")+1);
      try{
        JsonObject json = new JsonObject(Db.readDbContents());
        JsonArray services = json.getJsonArray("services");
        for(int i = 0;i < services.size();i++){
          JsonObject service = services.getJsonObject(i);
          if(service.getString("id").equals(id)){
            services.remove(i);
            break;
          }
        }
        json.put("services", services);
        Db.writeToDb(json);
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
