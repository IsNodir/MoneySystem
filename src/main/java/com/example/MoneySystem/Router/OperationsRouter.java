package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Model.OperationDTO;
import com.example.MoneySystem.Model.TransactionDTO;
import com.example.MoneySystem.Service.OperationsService;
import com.example.MoneySystem.Service.OperationsValidationHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class OperationsRouter extends RoutersAssistant{

  private final OperationsService operationsService;
  private final OperationsValidationHandler operationsValidationHandler;

  public OperationsRouter(OperationsService operationsService, OperationsValidationHandler operationsValidationHandler) {
    this.operationsService = operationsService;
    this.operationsValidationHandler = operationsValidationHandler;
  }

  @Override
  protected Router buildRouter(Router router, Vertx vertx, String authPath) {

    super.buildRouter(router, vertx, authPath);

    router.post("/new").handler(operationsValidationHandler.operationsNew()).handler(this::apiNew);
    router.delete("/delete").handler(operationsValidationHandler.operationsDelete()).handler(this::apiDelete);
    router.get("/history").handler(operationsValidationHandler.operationsHistory()).handler(this::apiHistory);
    router.post("/transaction").handler(operationsValidationHandler.operationsTransaction()).handler(this::apiTransaction);

    return router;
  }

  private void apiNew (RoutingContext ctx) {
    final OperationDTO operationDTO = ctx.getBodyAsJson().mapTo(OperationDTO.class);
    operationsService.insertOperation(operationDTO, ctx);
  }

  private void apiDelete (RoutingContext ctx) {
    final int id = ctx.getBodyAsJson().getInteger("id");
    final int idUser = ctx.getBodyAsJson().getInteger("id_user");
    operationsService.deleteOperation(id, idUser, ctx);
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
