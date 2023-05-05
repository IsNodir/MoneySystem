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

    CreateTables.create(postgreSQLContainer.getJdbcUrl());

    vertx.deployVerticle(new UsersVerticle(), testContext.succeeding(usersVerticleId -> testContext.completeNow()));
  }

  @Test
  @Order(1)
  @DisplayName("Create User")
  void createUser (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject()
      .put("login", "Brad")
      .put("password", "111333000");

    CreateAndAuthorizeUserTest.createUser(testContext, webClient, jsonObject);
  }

  @Test
  @Order(2)
  @DisplayName("Login User")
  void loginUser (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject()
      .put("login", "Brad")
      .put("password", "111333000");

    CreateAndAuthorizeUserTest.loginUser(testContext, webClient, webClientSession, jsonObject);
  }

  @Test
  @Order(3)
  @DisplayName("Protected Change Password User")
  void changePasswordUser (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject()
      .put("login", "Brad")
      .put("password", "111333000")
      .put("new_password", "579109090");

    webClientSession.put(8082, "localhost", "/api/v1/users/protected/change-password")
      .as(BodyCodec.string())
      .sendJsonObject(jsonObject)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() -> {
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertTrue(response.body().contains("Password updated successfully for"))
          );
          testContext.completeNow();
        });

      }));
  }

  @Test
  @Order(4)
  @DisplayName("Login User with new password")
  void loginUserWithNewPassword (VertxTestContext testContext) {
    JsonObject jsonObject = new JsonObject()
      .put("login", "Brad")
      .put("password", "579109090");

    CreateAndAuthorizeUserTest.loginUser(testContext, webClient, webClientSession, jsonObject);
  }

  @AfterAll
  static void end() {
    webClient.close();
    webClientSession.close();
  }
}
