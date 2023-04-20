package com.example.MoneySystem;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@ExtendWith(VertxExtension.class)
public class CreateAndAuthorizeUserTest {

//  @Test
//  @DisplayName("Create and authorize user")
  public static void initiateUser (VertxTestContext testContext, WebClient webClient, JsonObject userJson) {

    webClient.post(8082, "localhost", "/api/v1/users/create")
      .as(BodyCodec.string())
      .sendJsonObject(userJson)
      .onComplete(response -> {
        webClient.post(8082, "localhost", "/api/v1/users/login")
          .sendJsonObject(userJson)
          .onComplete(response2 -> {

            TestOperations.setWebClientSessionHeader(response2.result().getHeader("JWT"));
            testContext.completeNow();
          });
      })
      .onFailure(error -> {System.out.println(error.getMessage());});

//    webClient.post(8082, "localhost", "/api/v1/users/create")
//      .as(BodyCodec.string())
//      .sendJsonObject(userJson)
//      .onComplete(testContext.succeeding(response -> {
//        testContext.verify(() ->
//          Assertions.assertAll(
//            () -> Assertions.assertEquals(200, response.statusCode()),
//            () -> Assertions.assertEquals("User created successfully", response.body())
//          )
//        );
//
//        webClient.post(8082, "localhost", "/api/v1/users/login")
//          .sendJsonObject(userJson)
//          .onComplete(testContext.succeeding(response2 -> {
//            testContext.verify(() ->
//              Assertions.assertAll(
//                () -> Assertions.assertEquals(200, response2.statusCode()),
//                () -> Assertions.assertTrue(!response2.getHeader("JWT").toString().isEmpty())
//              )
//            );
//
//            TestOperations.setWebClientSessionHeader(response2.getHeader("JWT"));
//            testContext.completeNow();
//          }));
//      }))
//      .onFailure(error -> {System.out.println(error.getMessage());});
  }

//  @Test
//  @DisplayName("Create user")
  public static void createUser (VertxTestContext testContext, WebClient webClient, JsonObject userJson) {

    webClient.post(8082, "localhost", "/api/v1/users/create")
      .as(BodyCodec.string())
      .sendJsonObject(userJson)
      .onComplete(testContext.succeeding(response -> {
        testContext.verify(() ->
          Assertions.assertAll(
            () -> Assertions.assertEquals(200, response.statusCode()),
            () -> Assertions.assertEquals("User created successfully", response.body())
          )
        );
      }));
  }

}
