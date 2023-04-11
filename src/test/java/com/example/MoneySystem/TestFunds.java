package com.example.MoneySystem;

import com.example.MoneySystem.Verticles.FundsVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.FileWriter;
import java.io.IOException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(VertxExtension.class)
@Testcontainers
// extends ContainersEnvironment
public class TestFunds {

  private static WebClient webClient;

  @Container
  public static PostgreSQLContainer postgreSQLContainer = PostgresTestContainer.getInstance();

  @BeforeAll
  static void setup(Vertx vertx, VertxTestContext testContext) throws IOException{

    FileWriter writer = new FileWriter("src/main/resources/application-test.properties");

    writer.write("datasource.host=" + postgreSQLContainer.getHost() + "\n");
    writer.write("datasource.port=" + postgreSQLContainer.getFirstMappedPort() + "\n");
    writer.write("datasource.database=moneysystem2\n");
    writer.write("datasource.username=postgres\n");
    writer.write("datasource.password=db265\n");

    writer.close();

    webClient = WebClient.create(vertx);
    vertx.deployVerticle(new FundsVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

//  @Before
//  void vertxUp (Vertx vertx, VertxTestContext testContext) {
//    webClient = WebClient.create(vertx);
//    vertx.deployVerticle(new FundsVerticle(), testContext.succeeding(id -> testContext.completeNow()));
//  }

//  private static void startVerticle() {
//    VertxTestContext testContext = new VertxTestContext();
//    vertx.deployVerticle(new FundsVerticle(), testContext.succeeding(id -> testContext.completeNow()));
//  }


//  @BeforeAll
//  static void deployVerticle(Vertx vertx, VertxTestContext testContext) {
//    webClient = WebClient.create(vertx);
//    vertx.deployVerticle(new FundsVerticle(), testContext.succeeding(id -> testContext.completeNow()));
//  }

  @Test
  @Order(1)
  @DisplayName("Current Balance")
  void currentBalance (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id", 3);

    webClient.get(8081, "localhost", "/api/v1/balance/current")
      .as(BodyCodec.string())
      .putHeader("Authorization", "Bearer %s".formatted("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6IkFsbGEiLCJpYXQiOjE2Nzc2MDIyMTV9.u187eQvJGbxBCndT2zAdWkxgNUe8O97PeMHon0ju_3Q"))
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
          testContext.verify(() ->
            Assertions.assertAll(
              () -> Assertions.assertEquals(200, response.statusCode()),
              () -> Assertions.assertEquals("Balance: 10000.0", response.body())
              //readFileAsJsonObject("src/test/resources/funds/currentBalance.json")
            )
          );

          testContext.completeNow();
        }));
  }

  @Test
  @Order(2)
  @DisplayName("Balance History")
  void balanceHistory (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id_user", 1);
    jsonObject.put("dayFrom", "18.02.2023");
    jsonObject.put("dayTo", "15.03.2023");

    webClient.get(8081, "localhost", "/api/v1/balance/history")
      .as(BodyCodec.string())
      .putHeader("Authorization", "Bearer %s".formatted("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6IkFsbGEiLCJpYXQiOjE2Nzc2MDIyMTV9.u187eQvJGbxBCndT2zAdWkxgNUe8O97PeMHon0ju_3Q"))
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertTrue(response.body().contains("Date: 2023-03-04 Balance: 440000.0"))
          )
        );

        testContext.completeNow();
      }));
  }

  @AfterAll
  static void end() {
    webClient.close();
  }

//  private JsonObject readFileAsJsonObject(String path) throws IOException {
//    return new JsonObject(Files.lines(Paths.get(path), StandardCharsets.UTF_8).collect(Collectors.joining("\n")));
//  }

}






/** OLD CURRENT BALANCE */
//      .send(testContext.succeeding(response -> {
//          testContext.verify(() ->
//            Assertions.assertAll(
//              () -> Assertions.assertEquals(200, response.statusCode())
//              //() -> Assertions.assertEquals(readFileAsJsonObject("src/test/resources/funds/currentBalance.json"), response.body())
//            )
//          );
//
//          testContext.completeNow();
//        })
//      );


/** OLD */

//  @Test
//  void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
//    vertx.deployVerticle(new FundsVerticle(), testContext.succeeding(id -> {
//      HttpClient client = vertx.createHttpClient();
//      client.request(HttpMethod.GET, 8081, "localhost", "/api/v1/balance/current")
//        .compose(req -> req.send().compose(HttpClientResponse::body))
//        .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
//          assertThat(buffer.toString()).isEqualTo("Plop");
//          testContext.completeNow();
//        })));
//    }));
//  }
//
//
//  Vertx vertx = Vertx.vertx();
//
//  @Test
//  void start_http_server() throws Throwable {
//
//    VertxTestContext testContext = new VertxTestContext();
//
//    vertx.createHttpServer()
//      .requestHandler(req -> req.response().end())
//      .listen(8081)
//      .onComplete(testContext.succeedingThenComplete());
//
//    assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
//    if (testContext.failed()) {
//      throw testContext.causeOfFailure();
//    }
//  }


/** OLD GET FROM application.properties */

//JsonObject config = new JsonObject()
//  .put("datasource.host", postgreSQLContainer.getHost())
//  .put("datasource.port", postgreSQLContainer.getFirstMappedPort())
//  .put("datasource.database", "moneysystem2")
//  .put("datasource.username", "postgres")
//  .put("datasource.password", "db265");

//  ConfigStoreOptions fileStoreOptions = new ConfigStoreOptions()
//  .setType("file")
//  .setFormat("properties")
//  .setConfig(new JsonObject().put("path", "application.properties"))
//  .setOptional(false);
//
//  ConfigRetrieverOptions options = new ConfigRetrieverOptions()
//    .addStore(fileStoreOptions)
//    .setScanPeriod(-1) // disable scanning for changes
//    .setIncludeDefaultStores(true);
//
//  ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
//
//    retriever.getConfig(ar -> {
//      if (ar.succeeded()) {
//      JsonObject fileStoreConfig = ar.result();
//      // merge values from file store with values from config object
//      config.mergeIn(fileStoreConfig);
//      // use updated config
//      String updatedHost = config.getString("datasource.host");
//      String updatedPort = config.getString("datasource.host");
//      System.out.println("Updated host value: " + updatedHost);
//      } else {
//      System.out.println("Failed to retrieve configuration: " + ar.cause());
//      }
//      });
