package com.example.MoneySystem.Verticles;

import com.example.MoneySystem.Router.UsersRouter;
import com.example.MoneySystem.Service.ErrorHandler;
import com.example.MoneySystem.Service.UsersService;
import com.example.MoneySystem.Service.UsersValidationHandler;
import com.example.MoneySystem.Utils.DbUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;

public class UsersVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> promise) {

    final PgPool dbClient = DbUtils.buildDbClient(vertx);

    final UsersService usersService = new UsersService(dbClient);
    final UsersValidationHandler usersValidationHandler = new UsersValidationHandler(vertx);
    final UsersRouter usersRouter = new UsersRouter(vertx, usersService, usersValidationHandler);

    final Router router = Router.router(vertx);
    ErrorHandler.buildHandler(router);
    usersRouter.setRouter(router);

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
          System.out.println("Successful users verticle creation");
        } else {
          promise.fail(http.cause());
          System.out.println("Users verticle creation failed");
        }
      });
  }
}
