package com.example.MoneySystem.Service;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.validation.BadRequestException;

public class ErrorHandler {

  private ErrorHandler() {
  }

  public static void buildHandler(Router router) {
    router.errorHandler(400, rc -> {
      if (rc.failure() instanceof BadRequestException) {
          // Something went wrong while parsing/validating the body
          rc.request().response().setStatusCode(400)
            .end(String.format(new IllegalArgumentException("Request body is invalid \n") + rc.failure().getMessage()));
      }
    });
  }
}
