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

    vertx.createHttpClient().post(8080, "localhost", "/service")
    .handler(response -> {
      response.bodyHandler(body -> {
        context.assertTrue(body.toString().contains("POST"));
        async.complete();
      });
    })
    .end();
  }

  @Test
  public void testDelete(TestContext context) {
    final Async async = context.async();
    vertx.createHttpClient().delete(8080, "localhost", "/service")
    .handler(response -> {
      response.bodyHandler(body -> {
        context.assertTrue(body.toString().contains("DELETE"));
        async.complete();
      });
    })
    .end();
  }
}
