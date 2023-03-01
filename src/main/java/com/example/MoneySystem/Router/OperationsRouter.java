package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Model.OperationDTO;
import com.example.MoneySystem.Model.TransactionDTO;
import com.example.MoneySystem.Service.OperationsService;
import com.example.MoneySystem.Service.UsersValidationHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class OperationsRouter {

  private final Vertx vertx;
  private final OperationsService operationsService;
  private final UsersValidationHandler usersValidationHandler;

  public OperationsRouter(Vertx vertx, OperationsService operationsService, UsersValidationHandler usersValidationHandler) {
    this.vertx = vertx;
    this.operationsService = operationsService;
    this.usersValidationHandler = usersValidationHandler;
  }

  public void setRouter(Router router) {
    router.mountSubRouter("/api/v1/operations", buildUserRouter());
  }

  private Router buildUserRouter() {
    final Router router = Router.router(vertx);

    JWTAuth authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer("keyboard cat")));

    router.route().handler(BodyHandler.create());

    router.route("/*").handler(JWTAuthHandler.create(authProvider));
    router.post("/new").handler(this::apiNew);
    router.delete("/delete").handler(this::apiDelete);
    router.get("/history").handler(this::apiHistory);
    router.post("/transaction").handler(this::apiTransaction);

    return router;
  }

  private void apiNew (RoutingContext ctx) {
    final OperationDTO operationDTO = ctx.getBodyAsJson().mapTo(OperationDTO.class);
    operationsService.insertOperation(operationDTO, ctx);
  }

  private void apiDelete (RoutingContext ctx) {
    final int id = ctx.getBodyAsJson().getInteger("id");
    operationsService.deleteOperation(id, ctx);
  }

  private void apiHistory (RoutingContext ctx) {
    final DateDTO dateDTO = ctx.getBodyAsJson().mapTo(DateDTO.class);
    operationsService.selectOperationsByDate(dateDTO, ctx);
  }

  private void apiTransaction (RoutingContext ctx) {
    final TransactionDTO transactionDTO = ctx.getBodyAsJson().mapTo(TransactionDTO.class);
    operationsService.insertTransaction(transactionDTO, ctx);
  }
}
