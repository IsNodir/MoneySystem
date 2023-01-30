package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.User;
import com.example.MoneySystem.Service.UserService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class UserRouter {
  private final Vertx vertx;
  private final UserService userService;

  public UserRouter(Vertx vertx, UserService userService) {
    this.vertx = vertx;
    this.userService = userService;
  }

  public void setRouter(Router router) {
    router.mountSubRouter("/user", buildUserRouter());
  }

  private Router buildUserRouter(){
    final Router userRouter = Router.router(vertx);

    userRouter.route().handler(BodyHandler.create());

    userRouter.put("/change-password").handler(ctx -> {
      final User user = ctx.getBodyAsJson().mapTo(User.class);

      userService.updatePassword(user);

      ctx.response().putHeader("Content-Type", "application/json;charset=utf-8").end("Password successfully updated!");
    });

    userRouter.post("/create-user").handler(ctx -> {
      final User user = ctx.getBodyAsJson().mapTo(User.class);

      userService.createUser(user);

      ctx.response().putHeader("Content-Type", "application/json;charset=utf-8").end("User successfully created!");
    });

    //userRouter.post("")

    return userRouter;
  }

}
