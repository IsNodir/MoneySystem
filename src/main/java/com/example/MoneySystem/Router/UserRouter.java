package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.User;
import com.example.MoneySystem.Service.UserService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

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
    userRouter.get("/change-password").handler(ctx -> {

      final User user = ctx.getBodyAsJson().mapTo(User.class);

      userService.updatePassword(user);
    });

    return userRouter;
  }

}
