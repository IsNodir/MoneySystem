package com.example.MoneySystem.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {
    final long start = System.currentTimeMillis();

    deployApiVerticle(vertx)
      .onSuccess(success -> System.out.println(start))
      .onFailure(throwable -> throwable.getMessage());
  }

  private Future<String> deployApiVerticle(Vertx vertx) {
    return vertx.deployVerticle(ApiVerticle.class.getName());
  }

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
