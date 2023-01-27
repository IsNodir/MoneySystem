package com.example.MoneySystem.Verticles;

import com.example.MoneySystem.Repository.UserRepository;
import com.example.MoneySystem.Router.UserRouter;
import com.example.MoneySystem.Service.UserService;
import com.example.MoneySystem.Utils.DbUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) {
//    final long start = System.currentTimeMillis();
//
//    deployApiVerticle(vertx)
//      .onSuccess(success -> System.out.println(start))
//      .onFailure(throwable -> throwable.getMessage());

    final PgPool dbClient = DbUtils.buildDbClient(vertx);

    final UserRepository userRepository = new UserRepository();
    final UserService userService = new UserService(dbClient, userRepository);
    final UserRouter userRouter = new UserRouter(vertx, userService);

    final Router router = Router.router(vertx);
    userRouter.setRouter(router);

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
          System.out.println("Successful server creation");
        } else {
          promise.fail(http.cause());
          System.out.println("Server creation failed");
        }
      });
  }

//  private Future<String> deployApiVerticle(Vertx vertx) {
//    return vertx.deployVerticle(ApiVerticle.class.getName());
//  }

}

//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.Promise;
//import io.vertx.ext.web.Router;
//
//public class MainVerticle extends AbstractVerticle {
//
//  @Override
//  public void start(Promise<Void> startPromise) throws Exception {
//    Router router = Router.router(vertx);
//    router.get("/api").handler(ctx -> {
//      ctx.request().response().end("Hello from Vert.x!");
//    });
//    router.get("/api/:pathparam").handler(ctx -> {
//      String pathparam = ctx.pathParam("pathparam");
//      ctx.request().response().end(
//        String.format("Hello %s", pathparam));
//    });
//
//    vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
//      if (http.succeeded()) {
//        startPromise.complete();
//        System.out.println("HTTP server started on port 8888");
//      } else {
//        startPromise.fail(http.cause());
//      }
//    });
//
////    vertx.createHttpServer().requestHandler(req -> {
////      req.response()
////        .putHeader("content-type", "text/plain")
////        .end("Hello from Vert.x!");
////    }).listen(8080, http -> {
////      if (http.succeeded()) {
////        startPromise.complete();
////        System.out.println("HTTP server started on port 8888");
////      } else {
////        startPromise.fail(http.cause());
////      }
////    });
//  }
//}
