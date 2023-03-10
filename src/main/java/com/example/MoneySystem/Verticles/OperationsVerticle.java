package com.example.MoneySystem.Verticles;

import com.example.MoneySystem.Router.OperationsRouter;
import com.example.MoneySystem.Service.ErrorHandler;
import com.example.MoneySystem.Service.OperationsService;
import com.example.MoneySystem.Service.OperationsValidationHandler;
import com.example.MoneySystem.Utils.DbUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;

public class OperationsVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> promise) {

      final PgPool dbClient = DbUtils.buildDbClient(vertx);

      final OperationsService operationsService = new OperationsService(dbClient);
      final OperationsValidationHandler operationsValidationHandler = new OperationsValidationHandler(vertx);
      final OperationsRouter operationsRouter = new OperationsRouter(operationsService, operationsValidationHandler);

      final Router router = Router.router(vertx);
      ErrorHandler.buildHandler(router);
      operationsRouter.setRouter(router, "/api/v1/operations", "/*" ,vertx);

      buildHttpServer(vertx, promise, router);
    }

    private void buildHttpServer(Vertx vertx,
                                 Promise<Void> promise,
                                 Router router) {
      final int port = 8080;

      vertx.createHttpServer()
        .requestHandler(router)
        .listen(port, http -> {
          if (http.succeeded()) {
            promise.complete();
            System.out.println("Successful operations verticle creation");
          } else {
            promise.fail(http.cause());
            System.out.println("Operations verticle failed");
          }
        });
    }
}
