package com.example.MoneySystem;

import com.example.MoneySystem.Verticles.OperationsVerticle;
import com.example.MoneySystem.Verticles.UsersVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientSession;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(VertxExtension.class)
@Testcontainers
public class TestOperations {
  private static WebClient webClient;

  private static WebClientSession webClientSession;

  public static void setWebClientSessionHeader(String header) {
    webClientSession.addHeader("Authorization", "Bearer %s".formatted(header));
  }

  @Container
  public static PostgreSQLContainer postgreSQLContainer = PostgresTestContainer.getInstance();

  @BeforeAll
  static void setup(Vertx vertx, VertxTestContext testContext) {
    System.setProperty("DB_PORT", String.valueOf(postgreSQLContainer.getFirstMappedPort()));
    System.setProperty("DB_HOST", postgreSQLContainer.getHost());
    System.setProperty("DB_DATABASE", postgreSQLContainer.getDatabaseName());
    System.setProperty("DB_USERNAME", postgreSQLContainer.getUsername());
    System.setProperty("DB_PASSWORD", postgreSQLContainer.getPassword());

    webClient = WebClient.create(vertx);
    webClientSession = WebClientSession.create(webClient);

    vertx.deployVerticle(new UsersVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    vertx.deployVerticle(new OperationsVerticle(), testContext.succeeding(id -> testContext.completeNow()));

    CreateTables.create(postgreSQLContainer.getJdbcUrl());
  }

  @Test
  @Order(1)
  @DisplayName("Initiate user 1")
  void initiateUser (VertxTestContext testContext) {

    JsonObject userJson = new JsonObject();
    userJson.put("login", "Brad");
    userJson.put("password", "111333000");

    CreateAndAuthorizeUserTest.initiateUser(testContext, webClient, webClientSession, userJson);
  }

  @Test
  @Order(2)
  @DisplayName("Create user 2")
  void createUser (VertxTestContext testContext) {

    JsonObject userJson = new JsonObject();
    userJson.put("login", "Alice");
    userJson.put("password", "222444111");

    CreateAndAuthorizeUserTest.createUser(testContext, webClient, userJson);
  }

  @Test
  @Order(3)
  @DisplayName("New Operation")
  void newOperation (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id_user", 1);
    jsonObject.put("amount", 10000);
    jsonObject.put("date", "20.04.2023");
    jsonObject.put("is_operation", false);
    jsonObject.put("is_expense", false);

    webClientSession.post(8080, "localhost", "/api/v1/operations/new")
      .as(BodyCodec.string())
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
  @Order(4)
  @DisplayName("Operation History")
  void operationHistory2 (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id_user", 1);
    jsonObject.put("dayFrom", "18.02.2023");
    jsonObject.put("dayTo", "25.04.2023");

    webClientSession.get(8080, "localhost", "/api/v1/operations/history")
      .as(BodyCodec.string())
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertTrue(response.body().contains("Income ID: 1; Money_amount: +10000.0; Date: 2023-04-20"))
          )
        );

        testContext.completeNow();
      }));
  }

  @Test
  @Order(5)
  @DisplayName("Delete Operation")
  void deleteOperation (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id", 1);
    jsonObject.put("id_user", 1);

    webClientSession.delete(8080, "localhost", "/api/v1/operations/delete")
      .as(BodyCodec.string())
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
  @Order(6)
  @DisplayName("Operation History")
  void operationHistory (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id_user", 1);
    jsonObject.put("dayFrom", "18.02.2023");
    jsonObject.put("dayTo", "25.04.2023");

    webClientSession.get(8080, "localhost", "/api/v1/operations/history")
      .as(BodyCodec.string())
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode())
            //() -> Assertions.assertTrue(response.body().contains("Expense ID: 5; Money_amount: -6782.9; Date: 2023-03-01"))
          )
        );

        testContext.completeNow();
      }));
  }

  @Test
  @Order(7)
  @DisplayName("Transaction Operation")
  void transactionOperation (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("id_sender", 1);
    jsonObject.put("id_receiver", 2);
    jsonObject.put("amount", 10000);
    jsonObject.put("date", "20.04.2023");

    webClientSession.post(8080, "localhost", "/api/v1/operations/transaction")
      .as(BodyCodec.string())
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
    webClientSession.close();
  }
}
