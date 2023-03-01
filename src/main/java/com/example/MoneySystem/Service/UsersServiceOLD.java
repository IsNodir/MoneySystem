package com.example.MoneySystem.Service;

import com.example.MoneySystem.Model.*;
import com.example.MoneySystem.Repository.FundsRepositoryOld;
import com.example.MoneySystem.Repository.UsersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;

import java.util.List;

public class UsersServiceOLD {

  private final PgPool dbClient;

  private final UsersRepository usersRepository;

  private final FundsRepositoryOld fundsRepositoryOld;

  public UsersServiceOLD(PgPool dbClient, UsersRepository usersRepository, FundsRepositoryOld fundsRepositoryOld) {
    this.dbClient = dbClient;
    this.usersRepository = usersRepository;
    this.fundsRepositoryOld = fundsRepositoryOld;
  }

  public Future<Object> updatePassword(UsersDTO users) {

//    String password = usersRepository.selectByLogin(dbClient, users.getLogin()).result().getPassword();
//
//    if(password == users.getPassword())
//    {
//      Future<Object> future = new FailedFuture("You entered the same password");
//      return future;
//    }

    return usersRepository.updatePassword(dbClient, users);

//      dbClient.withTransaction(
//      connection -> {
//        userRepository.updatePassword(dbClient, user);
//        return null;
//      })
//      .onSuccess(v -> System.out.println("Transaction succeeded"))
//      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));

//      SqlTemplate
//        .forUpdate(dbClient, SQL_UPDATE)
//        .mapFrom(User.class)
//        .execute(user)
////        .flatMap(rowSet -> {
////          if (rowSet.rowCount() > 0) {
////            return Future.succeededFuture(user);
////          } else {
////            throw new NoSuchElementException(user.getLogin());
////          }
////        })
//        .onSuccess(res -> {
//          System.out.println("User updated");
//        })
//        .onFailure(res -> {
//          System.out.println("User NOT updated");
//        });
  }

  public Future<SqlResult<Void>> createUser(UsersDTO user) {

    return usersRepository.insert(dbClient, user);

//    String login = user.getLogin();
//    String password = user.getPassword();
//    client
//      .preparedQuery("INSERT INTO public.user (login, password) VALUES ($1, $2)")
//      .execute(Tuple.of(login, password), ar -> {
//        if (ar.succeeded()) {
//          RowSet<Row> rows = ar.result();
//          System.out.println(rows.rowCount());
//        } else {
//          System.out.println("Failure: " + ar.cause().getMessage());
//        }
//      });

//    SqlTemplate
//      .forUpdate(dbClient, "INSERT INTO public.user VALUES (#{login},#{password})")
//      .mapFrom(User.class)
//      .execute(user)
//      .onSuccess(res -> {
//        System.out.println("User inserted");
//      });
  }

  public Future<UsersDTO> searchByLogin(UsersDTO users) {

    return usersRepository.selectByLogin(dbClient, users.getLogin());

//     UsersDTO user = (UsersDTO) usersRepository.selectByLogin(dbClient, users.getLogin()).toCompletionStage();
//     return user;

//    CompletionStage<UsersDTO> user = usersRepository.selectByLogin(dbClient, users.getLogin()).toCompletionStage();

//    try {
//      usersRepository.selectByLogin(dbClient, users.getLogin()).wait();
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
//    AtomicReference<UsersDTO> sos = null;
//
//    usersRepository.selectByLogin(dbClient, users.getLogin()).onComplete(ar -> {
//      if(ar.succeeded())
//        sos.set(ar.result());
//      else
//        System.out.println(ar.cause());
//    });
//
//    UsersDTO user =sos.get();
//
//    return user;
  }

//  public Future<FundDTO> getBalance(String login, DateDTO date) {
//    return fundRepository.selectByLoginDate(dbClient, login, date);
//  }

  public Future<List<FundDTO>> getMoneyInfo(String login, DateDTO date) {
    return fundsRepositoryOld.selectByLoginDates(dbClient, login, date);
  }

  public Future<FundDTO> getCurrentBalanceId(String login) {
    return fundsRepositoryOld.selectCurrentBalanceID(dbClient, login);
  }

  public Future<FundDTO> getCurrentBalance(String login, int id) {
    return fundsRepositoryOld.selectCurrentBalance(dbClient, login, id);
  }

  public void sendMoney(String sender, OLDOperationDTO operation, RoutingContext ctx)
  {
    getCurrentBalanceId(sender).onComplete((AsyncResult<FundDTO> ar) -> {
      if (ar.succeeded()) {
        int id = ar.result().getId();
        getCurrentBalance(sender, id)
          .onComplete((AsyncResult<FundDTO> result) -> {
            if (result.succeeded()) {

              if (result.result().getBalance() >= operation.getMoney_amount()) {
                fundsRepositoryOld.sendMoney(dbClient, sender, operation, id)
                  .onSuccess(res -> {
                    ctx.request().response().end(String.format("Transaction succeeded"));})
                  .onFailure(res -> {
                    ctx.request().response().end(String.format("Transaction failed: " + res.getMessage()));});
//                getCurrentBalance(operation.getReceiver(), id)
//                  .onComplete((AsyncResult<FundDTO> res) -> {
//                    if(res.succeeded()){
//                      double senderNewBalance = result.result().getBalance() - operation.getMoney_amount();
//                      double receiverNewBalance = res.result().getBalance() + operation.getMoney_amount();
//                      fundRepository.sendMoney()
//                    } else {
//                      System.out.println("Failure: " + ar.cause().getMessage());
//                    }
//                  });
              } else {
                ctx.request().response().end(String.format("NOT enough money left on balance"));
              }

            } else {
              System.out.println("Failure: " + ar.cause().getMessage());
            }
          });
      } else {
          System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public Future<? extends ObjectReader> getDate (ObjectReader date) {
    try {
      return fundsRepositoryOld.selectDate(dbClient, date);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /** OLD DELETE OPERATION */
//  public void deleteOperation (OperationDTO operationDTO, RoutingContext ctx, String currentUser) {
//
//    if (currentUser.equals(operationDTO.getSender_login().toString())) {
//      fundRepository.updateSenderStatus(dbClient, operationDTO.getId())
//        .onSuccess(res -> {
//          fundRepository.deleteOperation(dbClient, operationDTO.getId())
//            .onSuccess(result -> {ctx.request().response().end(String.format("Operation deleted for both users"));})
//            .onFailure(result -> {ctx.request().response().end(String.format("Operation deleted for you only: " + result.getMessage()));});
//        })
//        .onFailure(res -> {ctx.request().response().end(String.format("Status change failed: " + res.getMessage()));});
//    } else if (currentUser.equals(operationDTO.getReceiver_login().toString())) {
//      fundRepository.updateReceiverStatus(dbClient, operationDTO.getId())
//        .onSuccess(res -> {
//          fundRepository.deleteOperation(dbClient, operationDTO.getId())
//            .onSuccess(result -> {ctx.request().response().end(String.format("Operation deleted for both users"));})
//            .onFailure(result -> {ctx.request().response().end(String.format("Operation deleted for you only: " + result.getMessage()));});
//        })
//        .onFailure(res -> {ctx.request().response().end(String.format("Status change failed: " + res.getMessage()));});
//    } else {ctx.request().response().setStatusCode(400).end(String.format("There is no operation with this sender or receiver"));}
//
//  }

  public void deleteExpense (int expense_id, RoutingContext ctx) {
    fundsRepositoryOld.selectExpenseIsOperationById(dbClient, expense_id)
      .onSuccess(result -> {

        if(result.iterator().next().is_operation())
        {
          fundsRepositoryOld.updateSenderDeleteStatus(dbClient, expense_id)
            .onSuccess(res -> {
              if (res.iterator().next().getBoolean("receiver_delete"))
              {
                fundsRepositoryOld.deleteOperation(dbClient, expense_id, res.iterator().next().getInteger("income_id"))
                  .onSuccess(res2 -> {ctx.request().response().end(String.format("Operation deleted successfully"));})
                  .onFailure(res2 -> {ctx.request().response().setStatusCode(400).end(String.format("Operation deletion failed: " + res2.getMessage()));});
              } else {
                ctx.request().response().end(String.format("Operation deleted for sender only"));
              }
            })
            .onFailure(res -> {ctx.request().response().setStatusCode(400).end(String.format("Expense deletion failed: " + res.getMessage()));});

        } else {
          fundsRepositoryOld.deleteExpense(dbClient, expense_id)
            .onSuccess(res -> {ctx.request().response().end(String.format("Expense deleted successfully"));})
            .onFailure(res -> {ctx.request().response().setStatusCode(400).end(String.format("Expense deletion failed: " + res.getMessage()));});
        }

      })
      .onFailure(result -> {ctx.request().response().setStatusCode(400).end();});
  }

  public void deleteIncome (int income_id, RoutingContext ctx) {
    fundsRepositoryOld.selectIncomeIsOperationById(dbClient, income_id)
      .onSuccess(result -> {

        if(result.iterator().next().is_operation())
        {
          fundsRepositoryOld.updateReceiverDeleteStatus(dbClient, income_id)
            .onSuccess(res -> {
              if (res.iterator().next().getBoolean("sender_delete"))
              {
                fundsRepositoryOld.deleteOperation(dbClient, res.iterator().next().getInteger("expense_id"), income_id)
                  .onSuccess(res2 -> {ctx.request().response().end(String.format("Operation deleted successfully"));})
                  .onFailure(res2 -> {ctx.request().response().setStatusCode(400).end(String.format("Operation deletion failed: " + res2.getMessage()));});
              } else {
                ctx.request().response().end(String.format("Operation deleted for receiver only"));
              }
            })
            .onFailure(res -> {ctx.request().response().setStatusCode(400).end(String.format("Income deletion failed: " + res.getMessage()));});

        } else {
          fundsRepositoryOld.deleteIncome(dbClient, income_id)
            .onSuccess(res -> {ctx.request().response().end(String.format("Income deleted successfully"));})
            .onFailure(res -> {ctx.request().response().setStatusCode(400).end(String.format("Income deletion failed: " + res.getMessage()));});
        }

      })
      .onFailure(result -> {ctx.request().response().setStatusCode(400).end();});
  }

  public void insertExpense (ExpensesDTO expense, RoutingContext ctx) {
    fundsRepositoryOld.insertExpense(dbClient, expense)
      .onSuccess(result -> {ctx.request().response().end(String.format("Expense inserted successfully"));})
      .onFailure(result -> {ctx.request().response().setStatusCode(400).end(String.format("Expense insertion failed: " + result.getMessage()));});
  }

  public void insertIncome (IncomesDTO income, RoutingContext ctx) {
    fundsRepositoryOld.insertIncome(dbClient, income)
      .onSuccess(result -> {ctx.request().response().end(String.format("Income inserted successfully"));})
      .onFailure(result -> {ctx.request().response().setStatusCode(400).end(String.format("Income insertion failed: " + result.getMessage()));});
  }

  public void selectExpensesIncomesByDate (String login, DateDTO date, RoutingContext ctx) {
    fundsRepositoryOld.selectExpensesIncomesByDate(dbClient, login, date)
      .onSuccess(res -> {
        List<ExpensesIncomesDTO> expensesIncomes = res.stream().toList();
        ctx.request().response().end(String.format(login + ": " + expensesIncomes.toString()));
      })
      .onFailure(res -> {
        ctx.request().response().setStatusCode(400).end(String.format("Error: " + res.getMessage()));
      });
  }

//  public void selectExpensesIncomesByDate (String login, DateDTO date, RoutingContext ctx) {
//    fundRepository.selectExpensesIncomesByDate(dbClient, login, date)
//      .onSuccess(res -> {
//
//        List<ExpensesIncomesDTO> expensesIncomesDTO = new ArrayList<>();
//
//        while (res.iterator().hasNext()) {
//          int id = res.iterator().next().getInteger("id");
//          double money_amount = res.iterator().next().getDouble("money_amount");
//          LocalDate day = res.iterator().next().getLocalDate("day");
//          boolean is_expense = res.iterator().next().getBoolean("is_expense");
//          expensesIncomesDTO.add(new ExpensesIncomesDTO(id, money_amount, day, is_expense));
//        }
//
//        ctx.request().response().end(String.format(login + ": " + expensesIncomesDTO.toString()));
//      })
//      .onFailure(res -> {ctx.request().response().setStatusCode(400).end(String.format("Error: " + res.getMessage()));});
//  }

  public void insertOperation (OLDOperationDTO operation, RoutingContext ctx) {
    fundsRepositoryOld.insertOperation(dbClient, operation)
      .onSuccess(result -> {ctx.request().response().end(String.format("Operation inserted successfully"));})
      .onFailure(result -> {ctx.request().response().setStatusCode(400).end(String.format("Operation insertion failed: " + result.getMessage()));});
  }

  public void selectDay (int id, RoutingContext ctx) {
    fundsRepositoryOld.selectDay(dbClient, id)
      .onSuccess(result -> {
        ctx.request().response().end(String.format(String.valueOf(result.iterator().next().getLocalDate("day"))));})
      .onFailure(result -> {ctx.request().response().setStatusCode(400).end(String.format("Operation insertion failed: " + result.getMessage()));});
  }

}

