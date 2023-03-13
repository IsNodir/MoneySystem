package com.example.MoneySystem.Router;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import lombok.Getter;

@Getter
public class RoutersAssistant {

  public void setRouter(Router router, String mountPoint, String authPath, Vertx vertx) {
    router.mountSubRouter(mountPoint, buildRouter(router, vertx, authPath));
  }

  protected Router buildRouter(Router router, Vertx vertx, String authPath) {

    JWTAuth authProvider = authProvider(vertx);

    router.route().handler(BodyHandler.create());

    router.route(authPath).handler(JWTAuthHandler.create(authProvider));

    return router;
  }

  public JWTAuth authProvider(Vertx vertx) {
    JWTAuth authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer("keyboard cat")));
    return authProvider;
  }
}
