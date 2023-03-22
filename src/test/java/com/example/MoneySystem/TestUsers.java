package com.example.MoneySystem;

import com.example.MoneySystem.Verticles.UsersVerticle;
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
public class TestUsers {

  private static WebClient webClient;

  @BeforeAll
  static void setup(Vertx vertx, VertxTestContext testContext) {
    webClient = WebClient.create(vertx);
    vertx.deployVerticle(new UsersVerticle(), testContext.succeeding(usersVerticleId ->
      testContext.completeNow()));
  }

  @Test
  @Order(1)
  @DisplayName("Login Users")
  void loginUser (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("login", "Aleksey");
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

        testContext.completeNow();
      }));
  }

  @Test
  @Order(2)
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
  @Order(3)
  @DisplayName("Protected Change Password User")
  void changePasswordUser (VertxTestContext testContext) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.put("login", "Brad");
    jsonObject.put("password", "111333000");
    jsonObject.put("new_password", "579109090");

    webClient.put(8082, "localhost", "/api/v1/users/protected/change-password")
      .as(BodyCodec.string())
      .putHeader("Authorization", "Bearer %s".formatted("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6IkFsbGEiLCJpYXQiOjE2Nzc2MDIyMTV9.u187eQvJGbxBCndT2zAdWkxgNUe8O97PeMHon0ju_3Q"))
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
  }
}
