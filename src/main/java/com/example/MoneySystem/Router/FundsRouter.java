package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Service.FundsService;
import com.example.MoneySystem.Service.FundsValidationHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class FundsRouter {
  private final Vertx vertx;
  private final FundsService fundsService;
  private final FundsValidationHandler fundsValidationHandler;

  public FundsRouter(Vertx vertx, FundsService fundsService, FundsValidationHandler fundsValidationHandler) {
    this.vertx = vertx;
    this.fundsService = fundsService;
    this.fundsValidationHandler = fundsValidationHandler;
  }

  public void setRouter(Router router) {
    router.mountSubRouter("/api/v1/balance", buildUserRouter());
  }

  private Router buildUserRouter() {
    final Router router = Router.router(vertx);

    JWTAuth authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer("keyboard cat")));

    router.route().handler(BodyHandler.create());

    router.route("/*").handler(JWTAuthHandler.create(authProvider));
    router.get("/current").handler(fundsValidationHandler.fundsCurrent()).handler(this::apiCurrent);
    router.get("/history").handler(fundsValidationHandler.fundsHistory()).handler(this::apiHistory);

    return router;
  }

  private void apiCurrent(RoutingContext ctx) {
    final int id = ctx.getBodyAsJson().getInteger("id");
    fundsService.selectCurrentBalance(id, ctx);
  }

  private void apiHistory(RoutingContext ctx) {
    final DateDTO dateDTO = ctx.getBodyAsJson().mapTo(DateDTO.class);
    fundsService.selectBalances(dateDTO, ctx);
  }
}
