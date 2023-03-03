package com.example.MoneySystem.Service;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Model.OperationDTO;
import com.example.MoneySystem.Model.TransactionDTO;
import com.example.MoneySystem.Repository.FundsRepository;
import com.example.MoneySystem.Repository.OperationsRepository;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class OperationsService {

  private final PgPool dbClient;

  private final OperationsRepository operationsRepository = new OperationsRepository();

  private final FundsRepository fundsRepository = new FundsRepository();

  public OperationsService(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  public void insertOperation (OperationDTO operationDTO, RoutingContext ctx) {
    fundsRepository.checkBalance(dbClient, operationDTO.getDate(), operationDTO.getIdUser())
      .onSuccess(res -> {

        // to check whether user has balance for the given Date
        if(res.iterator().hasNext()) {
          checkIsExpenseAndFinishForInsertOperation(operationDTO, ctx, res);
        } else {
          fundsRepository.selectCurrentBalance(dbClient, operationDTO.getId())
            .onSuccess(currentBalance -> {

              // To check whether balance is ever created for user or not
              if(currentBalance.iterator().hasNext()) {
                fundsRepository.insertBalance(dbClient, operationDTO.getIdUser(), operationDTO.getDate(),
                    currentBalance.iterator().next().getDouble("balance"))
                  .onSuccess(res2 -> {
                    checkIsExpenseAndFinishForInsertOperation(operationDTO, ctx, res2);
                  })
                  .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});
              } else {
                fundsRepository.insertBalance(dbClient, operationDTO.getIdUser(), operationDTO.getDate(), 0)
                  .onSuccess(res2 -> {
                    checkIsExpenseAndFinishForInsertOperation(operationDTO, ctx, res2);
                  })
                  .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});
              }

            })
            .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});
        }
      })
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});
  }

  // insertOperation uses it
  private void checkIsExpenseAndFinishForInsertOperation(OperationDTO operationDTO, RoutingContext ctx, RowSet<Row> res) {

    // To check whether it is expense
    if (operationDTO.isExpense()) {

      // To check whether balance is bigger than desired expense amount
      if ( (res.iterator().next().getDouble("balance") - operationDTO.getAmount()) >= 0) {
        operationsRepository.insertExpenseOperation(dbClient, operationDTO)
          .onSuccess(result -> {
            ctx.request().response().end(String.format("Operation inserted successfully"));
          })
          .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Operation insertion failed: " + error.getMessage()));});
      }
      else {
        ctx.request().response().setStatusCode(400).end(String.format("NOT enough money on balance"));
      }

    } else {
      operationsRepository.insertIncomeOperation(dbClient, operationDTO)
        .onSuccess(result -> {
          ctx.request().response().end(String.format("Operation inserted successfully"));
        })
        .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Operation insertion failed: " + error.getMessage()));});
    }
  }

  public void deleteOperation (int id, RoutingContext ctx) {
    operationsRepository.selectIsOperationIsExpense(dbClient, id)
      .onSuccess(result -> {
        if(result.iterator().next().getBoolean("is_operation")) {

          if (result.iterator().next().getBoolean("is_expense")) {
            operationsRepository.updateSenderDeleteStatus(dbClient, id)
              .onSuccess(res -> {

                if(res.iterator().next().getBoolean("receiver_delete")) {
                  deleteTransaction(ctx, res);
                } else {
                  ctx.request().response().end(String.format("Transaction deleted for you (sender) only"));
                }

              })
              .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Operation deletion failed: " + error.getMessage()));});
          } else {
            operationsRepository.updateReceiverDeleteStatus(dbClient, id)
              .onSuccess(res -> {

                if(res.iterator().next().getBoolean("sender_delete")) {
                  deleteTransaction(ctx, res);
                } else {
                  ctx.request().response().end(String.format("Transaction deleted for you (receiver) only"));
                }

              })
              .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Operation deletion failed: " + error.getMessage()));});
          }

        } else {
          operationsRepository.deleteOperation(dbClient, id)
            .onSuccess(res -> {ctx.request().response().end(String.format("Operation deleted successfully"));})
            .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Operation deletion failed: " + error.getMessage()));});
        }

      })
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Operation deletion failed: " + error.getMessage()));});
  }

  // deleteOperation uses it
  private void deleteTransaction(RoutingContext ctx, RowSet<Row> res) {
    operationsRepository.deleteTransaction(dbClient, res.iterator().next().getInteger("expense_id"),
        res.iterator().next().getInteger("income_id"))
      .onSuccess(output -> {ctx.request().response().end(String.format("Transaction deleted successfully"));})
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Transaction deletion failed: " + error.getMessage()));});
  }

  public void selectOperationsByDate (DateDTO dateDTO, RoutingContext ctx) {
    operationsRepository.selectOperationsByDate(dbClient, dateDTO)
      .onSuccess(res -> {
        List<OperationDTO> operationsList = new ArrayList<>();

          res.forEach(each -> {
            int id = each.getInteger("id");
            double amount = each.getDouble("amount");
            Date date = Date.valueOf(each.getLocalDate("date"));
            boolean is_expense = each.getBoolean("is_expense");
            operationsList.add(new OperationDTO(id, amount, date, is_expense));
          });

        ctx.request().response().end(String.format(operationsList.toString()));
      })
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Operation selection failed: " + error.getMessage()));});
  }

  public void insertTransaction (TransactionDTO transactionDTO, RoutingContext ctx) {
    fundsRepository.checkBalance(dbClient, transactionDTO.getDate(), transactionDTO.getIdSender())
      .onSuccess(res -> {

        // to check whether user has balance for the given Date
        if(res.iterator().hasNext()) {
          fundsRepository.checkBalance(dbClient, transactionDTO.getDate(), transactionDTO.getIdReceiver())
            .onSuccess(res2 -> {

              if (res2.iterator().hasNext()) {

              }else {

              }

            })
            .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});

        } else {
//          fundsRepository.selectCurrentBalance(dbClient, operationDTO.getId())
//            .onSuccess(currentBalance -> {
//
//              // To check whether balance is ever created for user or not
//              if(currentBalance.iterator().hasNext()) {
//                fundsRepository.insertBalance(dbClient, operationDTO.getIdUser(), operationDTO.getDate(),
//                    currentBalance.iterator().next().getDouble("balance"))
//                  .onSuccess(res2 -> {
//                    checkIsExpenseAndFinishForInsertOperation(operationDTO, ctx, res2);
//                  })
//                  .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});
//              } else {
//                fundsRepository.insertBalance(dbClient, operationDTO.getIdUser(), operationDTO.getDate(), 0)
//                  .onSuccess(res2 -> {
//                    checkIsExpenseAndFinishForInsertOperation(operationDTO, ctx, res2);
//                  })
//                  .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});
//              }
//
//            })
//            .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});
        }
      })
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format(error.getMessage()));});
  }

  private void forInsertTransaction (TransactionDTO transactionDTO, RoutingContext ctx) {
    operationsRepository.insertTransaction(dbClient, transactionDTO)
      .onSuccess(res -> {ctx.request().response().end(String.format("Transaction succeeded, ID:" + res.iterator().next().getInteger("id")));})
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Transaction failed: " + error.getMessage()));});
  }
}
