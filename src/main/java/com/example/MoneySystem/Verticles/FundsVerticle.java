package com.example.MoneySystem.Verticles;

import com.example.MoneySystem.Router.FundsRouter;
import com.example.MoneySystem.Service.ErrorHandler;
import com.example.MoneySystem.Service.FundsService;
import com.example.MoneySystem.Service.FundsValidationHandler;
import com.example.MoneySystem.Utils.DbUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;

public class FundsVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) {

    final PgPool dbClient = DbUtils.buildDbClient(vertx);

    final FundsService fundsService = new FundsService(dbClient);
    final FundsValidationHandler fundsValidationHandler = new FundsValidationHandler(vertx);
    final FundsRouter fundsRouter = new FundsRouter(vertx, fundsService, fundsValidationHandler);

    final Router router = Router.router(vertx);
    ErrorHandler.buildHandler(router);
    fundsRouter.setRouter(router);

    buildHttpServer(vertx, promise, router);
  }

  private void buildHttpServer(Vertx vertx,
                               Promise<Void> promise,
                               Router router) {
    final int port = 8082;

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, http -> {
        if (http.succeeded()) {
          promise.complete();
          System.out.println("Successful funds verticle creation");
        } else {
          promise.fail(http.cause());
          System.out.println("Funds verticle failed");
        }
      });
  }
}
