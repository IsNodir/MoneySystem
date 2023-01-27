package com.example.MoneySystem.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;

import com.example.MoneySystem.Utils.DbUtils;

public class ApiVerticle extends AbstractVerticle {

//  @Override
//  public void start(Promise<Void> promise) {
//    final PgPool dbClient = DbUtils.buildDbClient(vertx);
//
////    final BookRepository bookRepository = new BookRepository();
////    final BookService bookService = new BookService(dbClient, bookRepository);
////    final BookHandler bookHandler = new BookHandler(bookService);
////    final BookValidationHandler bookValidationHandler = new BookValidationHandler(vertx);
////    final BookRouter bookRouter = new BookRouter(vertx, bookHandler, bookValidationHandler);
//
//    final Router router = Router.router(vertx);
////    bookRouter.setRouter(router);
//
//    buildHttpServer(vertx, promise, router);
//  }
//
//  /**
//   * Run HTTP server on port 8888 with specified routes
//   *
//   * @param vertx   Vertx context
//   * @param promise Callback
//   * @param router  Router
//   */
//  private void buildHttpServer(Vertx vertx,
//                               Promise<Void> promise,
//                               Router router) {
//    final int port = 8888;
//
//    vertx.createHttpServer()
//      .requestHandler(router)
//      .listen(port, http -> {
//        if (http.succeeded()) {
//          promise.complete();
//          System.out.println("Successful server creation");
//        } else {
//          promise.fail(http.cause());
//          System.out.println("Server creation failed");
//        }
//      });
//  }

}
