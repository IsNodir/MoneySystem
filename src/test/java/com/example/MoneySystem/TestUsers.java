package com.example.MoneySystem;

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
public class TestUsers {

  private static WebClient webClient;

  private static WebClientSession webClientSession;

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

    vertx.deployVerticle(new UsersVerticle(), testContext.succeeding(usersVerticleId ->
      testContext.completeNow()));

    CreateTables.create(postgreSQLContainer.getJdbcUrl());
  }

  @Test
  @Order(1)
  @DisplayName("Create User")
  void createUser (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("login", "Brad");
    jsonObject.put("password", "111333000");

    webClient.post(8082, "localhost", "/api/v1/users/create")
      .as(BodyCodec.string())
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertEquals("User created successfully", response.body())
          )
        );

        testContext.completeNow();
      }));
  }

  @Test
  @Order(2)
  @DisplayName("Login Users")
  void loginUser (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("login", "Brad");
    jsonObject.put("password", "111333000");

    webClient.post(8082, "localhost", "/api/v1/users/login")
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertTrue(!response.getHeader("JWT").toString().isEmpty())
          )
        );

        webClientSession.addHeader("Authorization", "Bearer %s".formatted(response.getHeader("JWT")));
        testContext.completeNow();
      }));
  }

  @Test
  @Order(3)
  @DisplayName("Protected Change Password User")
  void changePasswordUser (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("login", "Brad");
    jsonObject.put("password", "111333000");
    jsonObject.put("new_password", "579109090");

    webClientSession.put(8082, "localhost", "/api/v1/users/protected/change-password")
      .as(BodyCodec.string())
      //.putHeader("Authorization", "Bearer %s".formatted("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6IkFsbGEiLCJpYXQiOjE2Nzc2MDIyMTV9.u187eQvJGbxBCndT2zAdWkxgNUe8O97PeMHon0ju_3Q"))
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertTrue(response.body().contains("Password updated successfully for"))
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
