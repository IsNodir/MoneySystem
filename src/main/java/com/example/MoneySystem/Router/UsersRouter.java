package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.UsersDTO;
import com.example.MoneySystem.Service.UsersService;
import com.example.MoneySystem.Service.UsersValidationHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class UsersRouter extends RoutersAssistant{
  private final Vertx vertx;
  private final UsersService usersService;
  private final UsersValidationHandler usersValidationHandler;

  public UsersRouter(Vertx vertx, UsersService usersService, UsersValidationHandler usersValidationHandler) {
    this.vertx = vertx;
    this.usersService = usersService;
    this.usersValidationHandler = usersValidationHandler;
  }

  @Override
  protected Router buildRouter(Router router, Vertx vertx, String authPath) {

    super.buildRouter(router, vertx, authPath);

    router.post("/login").handler(this::apiAuthenticate);
    router.post("/create").handler(usersValidationHandler.create()).handler(this::apiCreateUser);
    router.put("/protected/change-password").handler(usersValidationHandler.update()).handler(this::apiChangePassword);

    return router;
  }

  private  void  apiAuthenticate (RoutingContext ctx)
  {
    final UsersDTO user = ctx.getBodyAsJson().mapTo(UsersDTO.class);

    JWTAuth authProvider = authProvider(vertx);

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

    usersService.updatePassword(user)
      .onSuccess(res -> {
        ctx.request().response().end(String.format("Password updated successfully for " + user.getLogin()));
      })
      .onFailure(res -> {
        ctx.request().response().end(String.format("Password NOT updated for '" + user.getLogin() + "': " + res.getMessage()));
      });
  }

  private void apiCreateUser(RoutingContext ctx) {
    final UsersDTO usersDTO = ctx.getBodyAsJson().mapTo(UsersDTO.class);

    usersService.insertUser(usersDTO, ctx);
  }
}
