package com.example.MoneySystem;

import com.example.MoneySystem.Verticles.FundsVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(VertxExtension.class)
public class TestFunds {

  private static WebClient webClient;

  @BeforeAll
  static void setup(Vertx vertx, VertxTestContext testContext) {
    webClient = WebClient.create(vertx);
    vertx.deployVerticle(new FundsVerticle(), testContext.succeeding(fundsVerticleId ->
      testContext.completeNow()));
  }

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
