package com.example.MoneySystem.Router;

import com.example.MoneySystem.Model.*;
import com.example.MoneySystem.Service.UsersServiceOLD;
import com.example.MoneySystem.Service.UsersValidationHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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

import java.util.List;

public class UsersRouterOld {
  private final Vertx vertx;
  private final UsersServiceOLD usersService;
  private final UsersValidationHandler usersValidationHandler;

  public UsersRouterOld(Vertx vertx, UsersServiceOLD usersService, UsersValidationHandler usersValidationHandler) {
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
    router.route("/*").handler(JWTAuthHandler.create(authProvider));
    router.put("/change-password").handler(usersValidationHandler.update()).handler(this::apiChangePassword);
    router.post("/create-user").handler(usersValidationHandler.create()).handler(this::apiCreateUser);
    router.get("/balance").handler(this::apiBalance);
    router.get("/balance-details").handler(usersValidationHandler.balance()).handler(this::apiBalanceDetails);
    router.post("/send-operation").handler(this::apiSendOperation);
    router.get("/day").handler(this::apiDay);
    //router.delete("/delete-operation").handler(usersValidationHandler.deleteOperation()).handler(this::apiDeleteOperation);
    router.post("/spend-operation").handler(this::apiSpend);
    router.post("/receive-operation").handler(this::apiReceive);
    router.get("/payments/history").handler(this::apiPaymentsHistory);
    router.post("/transaction-operation").handler(this::apiTransaction);
    router.delete("/delete-expense").handler(this::apiDeleteExpense);
    router.delete("/delete-income").handler(this::apiDeleteIncome);
    router.get("/get-date").handler(this::apiGetDate);

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

  private void apiGetDate (RoutingContext ctx)
  {
    final int id = ctx.getBodyAsJson().getInteger("id");
    usersService.selectDay(id, ctx);
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

  private void apiBalance(RoutingContext ctx) {

    final String login = ctx.user().get("login").toString();

    usersService.getCurrentBalanceId(login)
      .onComplete((AsyncResult<FundDTO> ar) -> {
        if (ar.succeeded()) {
          int id = ar.result().getId();
          usersService.getCurrentBalance(login, id)
            .onSuccess(res -> {
              ctx.request().response().end(String.format(login + ": Balance = " + res.getBalance()));
            })
            .onFailure(res -> {
              ctx.request().response().end(String.format("Error: " + res.getMessage()));
            });
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  private void apiBalanceDetails(RoutingContext ctx) {

    final DateDTO dates = ctx.getBodyAsJson().mapTo(DateDTO.class);
    final String login = ctx.user().get("login").toString();

    usersService.getMoneyInfo(login, dates)
      .onSuccess(res -> {
        List<FundDTO> funds = res.stream().toList();
        ctx.request().response().end(String.format(login + ": " + funds.toString()));
      })
      .onFailure(res -> {
        ctx.request().response().end(String.format("Error: " + res.getMessage()));
      });
  }

  private void apiDay(RoutingContext ctx) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

//    final DayDTO day = ctx.getBodyAsJson().mapTo(DayDTO.class);
//    final ObjectReader date = mapper.readerForMapOf(ctx.getBodyAsJson().mapTo(DayDTO.class).getClass());
//    final ObjectReader date = mapper.readValue(ctx.getBodyAsJson().mapTo(DayDTO.class), DayDTO.class);
    final ObjectReader date = ctx.getBodyAsJson().mapTo(mapper.readerForMapOf(DayDTO.class).getClass());

    usersService.getDate(date)
      .onSuccess(res -> {
        try {
          ctx.request().response().end(String.format(res.readValue("day").toString()));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      })
      .onFailure(res -> {
        ctx.request().response().end(String.format("Error: " + res.getMessage()));
      });
  }

  private void apiSendOperation(RoutingContext ctx) {
    final OLDOperationDTO operation = ctx.getBodyAsJson().mapTo(OLDOperationDTO.class);
    final String sender = ctx.user().get("login").toString();

    usersService.sendMoney(sender, operation, ctx);
  }

//  private void apiDeleteOperation(RoutingContext ctx) {
//    final OperationDTO operation = ctx.getBodyAsJson().mapTo(OperationDTO.class);
//    final String currentUser = ctx.user().get("login").toString();
//
//    usersService.deleteOperation(operation, ctx, currentUser);
//  }

  private void apiDeleteIncome(RoutingContext ctx) {
    final int income_id = ctx.getBodyAsJson().mapTo(IncomesDTO.class).getId();

    usersService.deleteIncome(income_id, ctx);
  }

  private void apiDeleteExpense(RoutingContext ctx) {
    final int expense_id = ctx.getBodyAsJson().mapTo(ExpensesDTO.class).getId();

    usersService.deleteExpense(expense_id, ctx);
  }

  private void apiSpend(RoutingContext ctx) {
    final ExpensesDTO expense = ctx.getBodyAsJson().mapTo(ExpensesDTO.class);
    final String currentUser = ctx.user().get("login").toString();

    if (currentUser.equals(expense.getSender_login()))
      usersService.insertExpense(expense, ctx);
    else
      ctx.request().response().setStatusCode(400).end(String.format("Invalid login"));
  }

  private void apiReceive(RoutingContext ctx) {
    final IncomesDTO income = ctx.getBodyAsJson().mapTo(IncomesDTO.class);

    usersService.insertIncome(income, ctx);
  }

  private void apiPaymentsHistory(RoutingContext ctx) {
    final DateDTO date = ctx.getBodyAsJson().mapTo(DateDTO.class);
    final String currentUser = ctx.user().get("login").toString();

    usersService.selectExpensesIncomesByDate(currentUser, date, ctx);
  }

  private void apiTransaction(RoutingContext ctx) {
    final OLDOperationDTO operation = ctx.getBodyAsJson().mapTo(OLDOperationDTO.class);

    usersService.insertOperation(operation, ctx);
  }
}


/** Authenticate (prototype) wrong */

//    JWTAuthOptions authConfig = new JWTAuthOptions()
//      .setKeyStore(new KeyStoreOptions()
//        .setType("jceks")
//        .setPath("keystore.jceks")
//        .setPassword("secret"));

//JWTAuthHandler.create(authProvider)

//        .onFailure(res -> {
//          ctx.request().response().end(String.format("Searched NOT for '" + user.getLogin() + "': " + res.getMessage()));
//        });

//      try {
//        userFromDB.result().getPassword().wait();
//      } catch (InterruptedException e) {
//        throw new RuntimeException(e);
//      }

//      userFromDB.toCompletionStage();

//      if (
//        userFromDB.getLogin().equals(user.getLogin()) &&
//          userFromDB.getPassword().equals(user.getPassword())) {
//        ctx.request().response()
//          .end(authProvider.generateToken(new JsonObject().put("login", user.getLogin())));
//      } else {
//        ctx.fail(401);
//      }

/** Authenticate prototype end */

//  private void updateValidation(RoutingContext ctx)
//  {
//
//    try {
//      usersValidationHandler.update();
//    } catch (BodyProcessorException e)
//    {
//      System.out.println(e.getMessage());
////      ctx.request().response().end(String.format(e.getMessage()));
//    }
////    usersValidationHandler.update().handle(
////      (RoutingContext) ctx.request().response().end("Couldn't")
////    );
//
////    if(!(result.isEmpty()))
////      ctx.request().response().end(String.format(result));
//  }
