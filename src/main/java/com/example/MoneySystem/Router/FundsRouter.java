package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Service.FundsService;
import com.example.MoneySystem.Service.FundsValidationHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class FundsRouter extends RoutersAssistant{
  private final FundsService fundsService;
  private final FundsValidationHandler fundsValidationHandler;

  public FundsRouter(FundsService fundsService, FundsValidationHandler fundsValidationHandler) {
    this.fundsService = fundsService;
    this.fundsValidationHandler = fundsValidationHandler;
  }

  @Override
  protected Router buildRouter(Router router, Vertx vertx, String authPath) {

    super.buildRouter(router, vertx, authPath);

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
