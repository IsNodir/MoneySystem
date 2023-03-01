package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.UsersDTO;
import com.example.MoneySystem.Service.UsersService;
import com.example.MoneySystem.Service.UsersValidationHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class UsersRouter {
  private final Vertx vertx;
  private final UsersService usersService;
  private final UsersValidationHandler usersValidationHandler;

  public UsersRouter(Vertx vertx, UsersService usersService, UsersValidationHandler usersValidationHandler) {
    this.vertx = vertx;
    this.usersService = usersService;
    this.usersValidationHandler = usersValidationHandler;
  }

  public void setRouter(Router router) {
    router.mountSubRouter("/api/v1/users", buildUserRouter());
  }

  private Router buildUserRouter() {
    final Router router = Router.router(vertx);

    JWTAuth authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer("keyboard cat")));

    router.route().handler(BodyHandler.create());

    router.post("/login").handler(this::apiAuthenticate);
    router.post("/create").handler(usersValidationHandler.create()).handler(this::apiCreateUser);
    router.route("/*").handler(JWTAuthHandler.create(authProvider));
    router.put("/change-password").handler(usersValidationHandler.update()).handler(this::apiChangePassword);

    return router;
  }

  private  void  apiAuthenticate (RoutingContext ctx)
  {
    final UsersDTO user = ctx.getBodyAsJson().mapTo(UsersDTO.class);

    JWTAuth authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer("keyboard cat")));

    usersService.searchByLogin(user).onComplete( (AsyncResult<UsersDTO> ar) -> {
      if (ar.succeeded()) {
        if (
          ar.result().getLogin().equals(user.getLogin()) &&
            ar.result().getPassword().equals(user.getPassword())) {
          ctx.request().response()
            .putHeader("JWT", authProvider.generateToken(new JsonObject().put("login", user.getLogin()))).end();
        } else {
          ctx.fail(401);
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  private void apiChangePassword (RoutingContext ctx)
  {
    final UsersDTO user = ctx.getBodyAsJson().mapTo(UsersDTO.class);
    final String login = ctx.user().get("login").toString();

    usersService.updatePassword(user)
      .onSuccess(res -> {
        ctx.request().response().end(String.format("Password updated successfully for " + user.getLogin() + login));
      })
      .onFailure(res -> {
        ctx.request().response().end(String.format("Password NOT updated for '" + user.getLogin() + "': " + res.getMessage()));
      });
  }

  private void apiCreateUser(RoutingContext ctx) {
    final UsersDTO user = ctx.getBodyAsJson().mapTo(UsersDTO.class);

    usersService.createUser(user)
      .onSuccess(res -> {
        ctx.request().response().end(String.format("User created successfully: " + user.getLogin()));
      })
      .onFailure(res -> {
        ctx.request().response().end(String.format("User '" + user.getLogin() + "' NOT created: " + res.getMessage()));
      });
  }
}
