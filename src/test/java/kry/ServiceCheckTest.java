package kry;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@RunWith(VertxUnitRunner.class)
public class ServiceCheckTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(ServiceCheck.class.getName(),
        context.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }


  @Test
  public void testGet(TestContext context) {
    final Async async = context.async();

    vertx.createHttpClient().getNow(8080, "localhost", "/service",
     response -> {
      response.handler(body -> {
        JsonObject json = new JsonObject(body.toString());
        JsonArray services = json.getJsonArray("services");
        JsonObject service = services.getJsonObject(0);
        context.assertTrue("test service".equals(service.getString("name")));
        async.complete();
      });
    });
  }

  @Test
  public void testPost(TestContext context) {
    final Async async = context.async();
    String postData = "{\"name\":\"testservice\", \"url\":\"kry.se\"}";

    vertx.createHttpClient().post(8080, "localhost", "/service")
    .handler(response -> {
      response.bodyHandler(body -> {
        JsonObject json = new JsonObject(body.toString());
        context.assertTrue("testservice".equals(json.getString("name")));
        context.assertTrue("kry.se".equals(json.getString("url")));
        context.assertTrue(!json.getString("id").isEmpty());
        async.complete();
      });
    })
    .putHeader("Content-Length", String.valueOf(postData.length()))
    .write(postData)
    .end();
  }

  @Test
  public void testDelete(TestContext context) {
    final Async async = context.async();

    //first create a new service
    String postData = "{\"name\":\"testservice\", \"url\":\"kry.se\"}";

    vertx.createHttpClient().post(8080, "localhost", "/service")
    .handler(postResponse -> {
      postResponse.bodyHandler(postBody -> {
        System.out.println("postBody");
        JsonObject postJson = new JsonObject(postBody.toString());
        final String id = postJson.getString("id");
        //then remove it
        String deleteData = "{\"id\":\""+id+"\"}";
        vertx.createHttpClient().delete(8080, "localhost", "/service")
        .handler(deleteResponse -> {
          System.out.println("deleteResponse");
          context.assertTrue(deleteResponse.statusCode() == 200);
          //check that its actually deleted
          vertx.createHttpClient().getNow(8080, "localhost", "/service",
           getResponse -> {
            System.out.println("getResponse");
            getResponse.handler(body -> {
              System.out.println("getBody");
              JsonObject json = new JsonObject(body.toString());
              JsonArray services = json.getJsonArray("services");
              for(int i = 0;i < services.size();i++){
                JsonObject service = services.getJsonObject(i);
                if(service.getString("id").equals(id))
                  context.assertTrue(false);
              }
              async.complete();
            });
          });
        })
        .putHeader("Content-Length", String.valueOf(deleteData.length()))
        .write(deleteData)
        .end();

      });
    })
    .putHeader("Content-Length", String.valueOf(postData.length()))
    .write(postData)
    .end();
  }
}
