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
      HttpServerResponse response = routingContext.response();
      try{
        response.putHeader("content-type", "text/json; charset=utf-8");
        String dbContent = Db.readDbContents();
        response.end(dbContent);
      } catch(Exception e){
        response.setStatusCode(500);
        response.end("{services:[]}");
      }
    });

    router.route(HttpMethod.POST, "/service").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      routingContext.request().bodyHandler(body -> {
          JsonObject jsonObject = createNewServiceFromRequest(body.toString());
          backgroundService.checkOne(jsonObject, newService ->
          {
            try{
              saveNewService(newService);
              response.putHeader("content-type", "text/json; charset=utf-8");
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
      try{
        deleteService(routingContext.normalisedPath());
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
      if (result.succeeded())
        fut.complete();
      else
        fut.fail(result.cause());
    });
  }

  private void saveNewService(JsonObject newService) throws Exception{
      JsonObject dbContents = new JsonObject(Db.readDbContents());
      JsonArray services = dbContents.getJsonArray("services");
      services.add(newService);
      dbContents.put("services", services);
      Db.writeToDb(dbContents);
  }

  private JsonObject createNewServiceFromRequest(String request){
    JsonObject jsonObject = new JsonObject(request);
    jsonObject.put("id", java.util.UUID.randomUUID().toString());
    String url = jsonObject.getString("url");
    if(!(url.startsWith("http://")))
      url = "http://" + url;
    jsonObject.put("url", url);
    return jsonObject;
  }

  private void deleteService(String uri) throws Exception{
    //id is last part of request URL
    String id = uri.substring(uri.lastIndexOf("/")+1);
    JsonObject json = new JsonObject(Db.readDbContents());
    JsonArray services = json.getJsonArray("services");
    json.put("services", removeElementWithId(services, id));
    Db.writeToDb(json);
  }

  private JsonArray removeElementWithId(JsonArray array, String id){
    for(int i = 0;i < array.size();i++){
      JsonObject obj = array.getJsonObject(i);
      if(id.equals(obj.getString("id"))){
        array.remove(i);
        break;
      }
    }
    return array;
  }
}
