package com.example.MoneySystem;

import com.example.MoneySystem.Verticles.OperationsVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(VertxExtension.class)
public class TestOperations {
  private static WebClient webClient;

  @BeforeAll
  static void setup(Vertx vertx, VertxTestContext testContext) {
    webClient = WebClient.create(vertx);
    vertx.deployVerticle(new OperationsVerticle(), testContext.succeeding(operationsVerticleId ->
      testContext.completeNow()));
  }

  @Test
  @Order(1)
  @DisplayName("New Operation")
  void newOperation (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id_user", 1);
    jsonObject.put("amount", 10000);
    jsonObject.put("date", "22.03.2023");
    jsonObject.put("is_operation", false);
    jsonObject.put("is_expense", true);

    webClient.post(8080, "localhost", "/api/v1/operations/new")
      .as(BodyCodec.string())
      .putHeader("Authorization", "Bearer %s".formatted("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6IkFsbGEiLCJpYXQiOjE2Nzc2MDIyMTV9.u187eQvJGbxBCndT2zAdWkxgNUe8O97PeMHon0ju_3Q"))
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertEquals("Operation inserted successfully", response.body())
          )
        );

        testContext.completeNow();
      }));
  }

  @Test
  @Order(2)
  @DisplayName("Delete Operation")
  void deleteOperation (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id", 75);
    jsonObject.put("id_user", 1);

    webClient.delete(8080, "localhost", "/api/v1/operations/delete")
      .as(BodyCodec.string())
      .putHeader("Authorization", "Bearer %s".formatted("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6IkFsbGEiLCJpYXQiOjE2Nzc2MDIyMTV9.u187eQvJGbxBCndT2zAdWkxgNUe8O97PeMHon0ju_3Q"))
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertTrue(response.body().equals("Operation deleted successfully")
              || response.body().equals("Transaction deleted by you (receiver) only")
              || response.body().equals("Transaction deleted by you (sender) only")
              || response.body().equals("Transaction deleted successfully"))
          )
        );

        testContext.completeNow();
      }));
  }

  @Test
  @Order(3)
  @DisplayName("Operation History")
  void operationHistory (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id_user", 1);
    jsonObject.put("dayFrom", "18.02.2023");
    jsonObject.put("dayTo", "23.03.2023");

    webClient.get(8080, "localhost", "/api/v1/operations/history")
      .as(BodyCodec.string())
      .putHeader("Authorization", "Bearer %s".formatted("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6IkFsbGEiLCJpYXQiOjE2Nzc2MDIyMTV9.u187eQvJGbxBCndT2zAdWkxgNUe8O97PeMHon0ju_3Q"))
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertTrue(response.body().contains("Expense ID: 5; Money_amount: -6782.9; Date: 2023-03-01"))
          )
        );

        testContext.completeNow();
      }));
  }

  @Test
  @Order(4)
  @DisplayName("Transaction Operation")
  void transactionOperation (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id_sender", 1);
    jsonObject.put("id_receiver", 3);
    jsonObject.put("amount", 10000);
    jsonObject.put("date", "22.03.2023");

    webClient.post(8080, "localhost", "/api/v1/operations/transaction")
      .as(BodyCodec.string())
      .putHeader("Authorization", "Bearer %s".formatted("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6IkFsbGEiLCJpYXQiOjE2Nzc2MDIyMTV9.u187eQvJGbxBCndT2zAdWkxgNUe8O97PeMHon0ju_3Q"))
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertTrue(response.body().contains("Transaction succeeded, ID:"))
          )
        );

        testContext.completeNow();
      }));
  }

  @AfterAll
  static void end() {
    webClient.close();
  }
}
