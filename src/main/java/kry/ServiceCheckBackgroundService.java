package kry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.core.Vertx;
import io.vertx.core.Handler;

import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.CountDownLatch;

public class ServiceCheckBackgroundService {
  private Vertx vertx;

  public ServiceCheckBackgroundService(Vertx x){
    vertx = x;
  }

  public void Run(){

    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {

      @Override
      public void run() {
        try{
          checkAll();
        } catch(Exception e){
          System.out.println("Background service fetch failed :" + e.toString());
        }
      }
    }, 0, 60000);
  }


  public void checkOne(final JsonObject obj, Handler<JsonObject> callback){
    LocalDateTime date = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    vertx.createHttpClient().getAbs(obj.getString("url") +":80",
     response -> {
       boolean isOk = response.statusCode() == 200;
       obj.put("status", isOk ? "OK" : "NOT OK");
       obj.put("lastCheck", date.format(formatter));
       callback.handle(obj);
    }).exceptionHandler(exception -> {
      obj.put("status", "NOT OK");
      obj.put("lastCheck", date.format(formatter));
      callback.handle(obj);
    }).end();
  }

  public void checkAll() throws Exception{
    JsonObject contents = new JsonObject(Db.readDbContents());
    JsonObject json = new JsonObject(Db.readDbContents());
    final JsonArray services = json.getJsonArray("services");
    CountDownLatch latch = new CountDownLatch(services.size());
    for(int i = 0;i < services.size();i++){
      JsonObject service = services.getJsonObject(i);
      final int index = i;
      checkOne(service, updatedService ->{
        services.getJsonObject(index).put("status", updatedService.getString("status"));
        services.getJsonObject(index).put("lastCheck", updatedService.getString("lastCheck"));
        latch.countDown();
      });
    }
    latch.await();
    json.put("services", services);
    Db.writeToDb(json);
  }
}
