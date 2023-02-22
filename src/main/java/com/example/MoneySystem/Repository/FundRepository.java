package com.example.MoneySystem.Repository;

import com.example.MoneySystem.Model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.*;

public class FundRepository {
  private static final String SQL_SELECT_BALANCE_BY_LOGIN_DATES = "SELECT balance, spent, received FROM public.funds f INNER JOIN public.dates d ON d.id = f.date_id WHERE user_login = #{login} AND (day BETWEEN #{dayFrom} AND #{dayTo})";
  private static final String SQL_SELECT_BALANCE_BY_LOGIN_DATE = "SELECT balance FROM public.funds, public.users, public.dates WHERE login = #{login} AND day BETWEEN #{dayFrom} AND #{dayTo}";
  private static final String SQL_SELECT_CURRENT_BALANCE_ID = "SELECT max(id) as id FROM public.funds WHERE user_login = #{login}";
  private static final String SQL_SELECT_CURRENT_BALANCE = "SELECT balance FROM public.funds WHERE user_login = #{login} AND id = #{id}";
  private static final String SQL_UPDATE_BALANCE_SENDER = "UPDATE public.funds SET balance = (balance - #{money_amount}) WHERE user_login = #{login} AND id = #{id}";
  private static final String SQL_UPDATE_BALANCE_RECEIVER = "UPDATE public.funds SET balance = (balance + #{money_amount}) WHERE user_login = #{login} AND id = #{id}";
  private static final String SQL_SELECT_DATE = "SELECT id FROM public.dates WHERE day = #{day}";
  private static final String SQL_UPDATE_SENDER_STATUS = "UPDATE public.operation SET sender_delete = true WHERE id = #{id}";
  private static final String SQL_UPDATE_RECEIVER_STATUS = "UPDATE public.operation SET receiver_delete = true WHERE id = #{id}";

//  private static final String SQL_DELETE_OPERATION = "DELETE FROM public.operation WHERE id = #{id} AND sender_delete = true AND receiver_delete = true";
  private static final String SQL_INSERT_EXPENSE = "INSERT INTO public.expenses (sender_login, money_amount, date_id, is_operation) " +
    "VALUES (#{sender_login}, #{money_amount}, #{date_id}, #{is_operation}) RETURNING id";
  private static final String SQL_INSERT_INCOME = "INSERT INTO public.incomes (receiver_login, money_amount, date_id, is_operation) " +
    "VALUES (#{receiver_login}, #{money_amount}, #{date_id}, #{is_operation}) RETURNING id";
  private static final String SQL_SELECT_EXPENSE_INCOME_BY_DATE = "(SELECT e.id, money_amount, date_id, is_expense FROM public.expenses e INNER JOIN public.dates d ON d.id = e.date_id WHERE sender_login = #{login} AND day BETWEEN #{dayFrom} AND #{dayTo})" +
    "UNION ALL (SELECT i.id, money_amount, date_id, is_expense FROM public.incomes i INNER JOIN public.dates d ON d.id = i.date_id WHERE receiver_login = #{login} AND day BETWEEN #{dayFrom} AND #{dayTo}) ORDER BY date_id";
  private static final String SQL_SELECT_EXPENSE_IS_OPERATION = "SELECT is_operation FROM public.expenses WHERE id = #{id}";
  private static final String SQL_SELECT_INCOME_IS_OPERATION = "SELECT is_operation FROM public.incomes WHERE id = #{id}";
  private static final String SQL_UPDATE_SENDER_DELETE_STATUS = "UPDATE public.operation SET sender_delete = true WHERE expense_id = $1 RETURNING receiver_delete, expense_id, income_id";
  private static final String SQL_UPDATE_RECEIVER_DELETE_STATUS = "UPDATE public.operation SET receiver_delete = true WHERE income_id = $1 RETURNING sender_delete, expense_id, income_id";

  private static final String SQL_INSERT_EXPENSE_FOR_OPERATION = "INSERT INTO public.expenses (sender_login, money_amount, date_id, is_operation) " +
    "VALUES ($1, $2, $3, $4) RETURNING id";
  private static final String SQL_INSERT_INCOME_FOR_OPERATION = "INSERT INTO public.incomes (receiver_login, money_amount, date_id, is_operation) " +
    "VALUES ($1, $2, $3, $4) RETURNING id";
  private static final String SQL_INSERT_OPERATION = "INSERT INTO public.operation (expense_id, income_id) " +
    "VALUES ($1, $2)";

  private static final String SQL_DELETE_EXPENSE = "DELETE FROM public.expenses WHERE id = $1";
  private static final String SQL_DELETE_INCOME = "DELETE FROM public.incomes WHERE id = $1";

  public FundRepository() {
  }

  public Future<SqlResult<Void>> insertExpense(PgPool dbClient, ExpensesDTO expenses) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_INSERT_EXPENSE)
      .mapFrom(ExpensesDTO.class)
      .execute(expenses)
      .onSuccess(res -> {System.out.println("Expense inserted successfully");})
      .onFailure(res -> {System.out.println("Expense insertion failed: " + res.getMessage());});
  }

  public Future<SqlResult<Void>> insertIncome(PgPool dbClient, IncomesDTO incomes) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_INSERT_INCOME)
      .mapFrom(IncomesDTO.class)
      .execute(incomes)
      .onSuccess(res -> {System.out.println("Income inserted successfully");})
      .onFailure(res -> {System.out.println("Income insertion failed: " + res.getMessage());});
  }

  /** Operation - is transaction of money between users. */
  // for creating new operation (transaction of money between users)
  public Future<String> insertOperation(PgPool dbClient, OperationDTO operation) {

    return dbClient.withTransaction(client -> client
        .preparedQuery(SQL_INSERT_EXPENSE_FOR_OPERATION)
        .execute(Tuple.of(operation.getSender_login(), operation.getMoney_amount(), operation.getDate_id(), true))
        .flatMap(res -> client
          .preparedQuery(SQL_INSERT_INCOME_FOR_OPERATION)
          .execute(Tuple.of(operation.getReceiver_login(), operation.getMoney_amount(), operation.getDate_id(), true))
          .flatMap(res2 ->
            client
              .preparedQuery(SQL_INSERT_OPERATION)
              .execute(Tuple.of(res.iterator().next().getInteger("id"), res2.iterator().next().getInteger("id")))
              .map("Operation inserted")
          )))
      .onSuccess(v -> System.out.println("Transaction succeeded"))
      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
  }

  /** Operation - is transaction of money between users. */
  public Future<String> deleteOperation(PgPool dbClient, int expense_id, int income_id) {

    return dbClient.withTransaction(client -> client
        .preparedQuery(SQL_DELETE_EXPENSE)
        .execute(Tuple.of(expense_id))
        .flatMap(res -> client
          .preparedQuery(SQL_DELETE_INCOME)
          .execute(Tuple.of(income_id))
          .map("Operation deleted")))
      .onSuccess(v -> System.out.println("Delete Operation succeeded"))
      .onFailure(err -> System.out.println("Delete Operation failed: " + err.getMessage()));
  }

  public Future<RowSet<Row>> deleteExpense(PgPool dbClient, int id) {
    return dbClient
      .preparedQuery(SQL_DELETE_EXPENSE)
      .execute(Tuple.of(id))
      .onSuccess(v -> System.out.println("Expense delete succeeded"))
      .onFailure(err -> System.out.println("Expense delete failed: " + err.getMessage()));
  }

  public Future<RowSet<Row>> deleteIncome(PgPool dbClient, int id) {
    return dbClient
      .preparedQuery(SQL_DELETE_INCOME)
      .execute(Tuple.of(id))
      .onSuccess(v -> System.out.println("Income delete succeeded"))
      .onFailure(err -> System.out.println("Income delete failed: " + err.getMessage()));
  }

  public Future<RowSet<Row>> updateReceiverDeleteStatus(PgPool dbClient, int income_id) {
    return dbClient
      .preparedQuery(SQL_UPDATE_RECEIVER_DELETE_STATUS)
      .execute(Tuple.of(income_id))
      .onSuccess(res -> {System.out.println("Receiver Delete status update is successful");})
      .onFailure(res -> {System.out.println("Receiver Delete status update failed: " + res.getMessage());});
  }

  public Future<RowSet<Row>> updateSenderDeleteStatus(PgPool dbClient, int expense_id) {
    return dbClient
      .preparedQuery(SQL_UPDATE_SENDER_DELETE_STATUS)
      .execute(Tuple.of(expense_id))
      .onSuccess(res -> {System.out.println("Sender Delete status update is successful");})
      .onFailure(res -> {System.out.println("Sender Delete status update failed: " + res.getMessage());});
  }

  public Future<RowSet<ExpensesDTO>> selectExpenseIsOperationById (PgPool dbClient, int expense_id) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_SELECT_EXPENSE_IS_OPERATION)
      .mapTo(ExpensesDTO.class)
      .execute(Collections.singletonMap("id", expense_id))
      .onSuccess(res -> {
        System.out.println("Successful is_operation selection");
      })
      .onFailure(res -> {
        System.out.println("is_operation selection failed: " + res.getMessage());
      });
  }

  public Future<RowSet<IncomesDTO>> selectIncomeIsOperationById (PgPool dbClient, int income_id) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_SELECT_INCOME_IS_OPERATION)
      .mapTo(IncomesDTO.class)
      .execute(Collections.singletonMap("id", income_id))
      .onSuccess(res -> {
        System.out.println("Successful is_operation selection");
      })
      .onFailure(res -> {
        System.out.println("is_operation selection failed: " + res.getMessage());
      });
  }

  public Future<List<ExpensesIncomesDTO>> selectExpensesIncomesByDate (PgPool dbClient, String login, DateDTO date) {

    JsonObject object = new JsonObject()
      .put("login", login)
      .put("dayFrom", date.getDayFrom().toLocalDate())
      .put("dayTo", date.getDayTo().toLocalDate());

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_EXPENSE_INCOME_BY_DATE)
      .mapTo(ExpensesIncomesDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {
        final List<ExpensesIncomesDTO> expensesIncomes = new ArrayList<>();
        rowSet.forEach(expensesIncomes::add);
        return expensesIncomes;
      })
      .onFailure(error -> System.out.println("Expenses and Incomes for this user NOT found: " + error.getMessage()));
  }

  public Future<? extends ObjectReader> selectDate (PgPool dbClient, ObjectReader date) throws JsonProcessingException {

//    LocalDate day = date.getDay();

    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_DATE)
      .mapTo(mapper.readerForMapOf(DayDTO.class).getClass())
      .execute(Collections.singletonMap("day", date.readValue("day").toString()))
      .map(rowSet -> {
        final RowIterator<? extends ObjectReader> iterator = rowSet.iterator();
        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException();
        }
      })
      .onFailure(error -> System.out.println("Day is NOT found: " + error.getMessage()));
  }

  public Future<FundDTO> selectByLoginDate (PgPool dbClient, String login, DateDTO date) {

    JsonObject object = new JsonObject()
      .put("login", login)
      .put("dayFrom", date.getDayFrom().toLocalDate())
      .put("dayTo", date.getDayTo().toLocalDate());

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_BALANCE_BY_LOGIN_DATE)
      .mapTo(FundDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {
        final RowIterator<FundDTO> iterator = rowSet.iterator();

        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException(login);
        }
      })
      .onFailure(error -> System.out.println("Balance for this User and Day is NOT found: " + error.getMessage()));
  }

  public Future<List<FundDTO>> selectByLoginDates (PgPool dbClient, String login, DateDTO date) {

    JsonObject object = new JsonObject()
      .put("login", login)
      .put("dayFrom", date.getDayFrom().toLocalDate())
      .put("dayTo", date.getDayTo().toLocalDate());

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_BALANCE_BY_LOGIN_DATES)
      .mapTo(FundDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {

        final List<FundDTO> funds = new ArrayList<>();
        rowSet.forEach(funds::add);

//        while (rowSet.iterator().hasNext()) {
//           FundDTO fundDTO = new FundDTO(rowSet.iterator().next().getBalance(),
//             rowSet.iterator().next().getSpent(),
//             rowSet.iterator().next().getReceived(),
//             rowSet.iterator().next().getDay());
//
//           funds.add(fundDTO);
//        }

        return funds;
      })
      .onFailure(error -> System.out.println("Balance for this User and Day is NOT found: " + error.getMessage()));
  }

  public Future<FundDTO> selectCurrentBalanceID (PgPool dbClient, String login) {
    JsonObject object = new JsonObject()
      .put("login", login);

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_CURRENT_BALANCE_ID)
      .mapTo(FundDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {
        final RowIterator<FundDTO> iterator = rowSet.iterator();

        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException(login);
        }
      })
      .onFailure(error -> System.out.println("Balance ID for this User NOT found: " + error.getMessage()));
  }

  public Future<FundDTO> selectCurrentBalance (PgPool dbClient, String login, int id) {
    JsonObject object = new JsonObject()
      .put("login", login)
      .put("id", id);

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_CURRENT_BALANCE)
      .mapTo(FundDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {
        final RowIterator<FundDTO> iterator = rowSet.iterator();

        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException(login);
        }
      })
      .onFailure(error -> System.out.println("Balance for this User NOT found: " + error.getMessage()));
  }

  public Future<Object> sendOperation(PgPool dbClient, String sender, int id, OperationDTO operation) {

    JsonObject senderExecute = new JsonObject()
      .put("login", sender)
      .put("id", id)
      .put("money_amount", operation.getMoney_amount());

    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE_BALANCE_SENDER)
      .execute(senderExecute.getMap())
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          return Future.failedFuture(new NoSuchElementException());
        }
      })
      .onSuccess(res -> {
        System.out.println("Send operation is successful");
      })
      .onFailure(res -> {
        System.out.println("Send operation failed: " + res.getMessage());
      });
  }

  public Future<Object> receiveOperation(PgPool dbClient, OperationDTO operation, int id) {

    JsonObject receiverExecute = new JsonObject()
      .put("login", operation.getReceiver_login())
      .put("id", id)
      .put("money_amount", operation.getMoney_amount());

    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE_BALANCE_RECEIVER)
      .execute(receiverExecute.getMap())
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          return Future.failedFuture(new NoSuchElementException());
        }
      })
      .onSuccess(res -> {
        System.out.println("Receive operation is successful");
      })
      .onFailure(res -> {
        System.out.println("Receive operation failed: " + res.getMessage());
      });
  }

  public Future<String> sendMoney (PgPool dbClient, String sender, OperationDTO operation, int id) {

//    JsonObject senderExecute = new JsonObject()
//      .put("login", sender)
//      .put("id", id);
//
//    JsonObject receiverExecute = new JsonObject()
//      .put("login", operation.getReceiver())
//      .put("id", id);

    return dbClient.withTransaction(client -> sendOperation(dbClient, sender, id, operation)
        .flatMap(res -> receiveOperation(dbClient, operation, id)
          .map("Transaction is successful")))
      .onSuccess(v -> System.out.println("Transaction succeeded"))
      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));

//    return dbClient.withTransaction(client -> client
//        .query(SQL_UPDATE_BALANCE_SENDER)
//        .execute()
//        .flatMap(res -> client
//          .query(SQL_UPDATE_BALANCE_RECEIVER)
//          .execute()
//          .map("Transaction is successful")))
//      .onSuccess(v -> System.out.println("Transaction succeeded"))
//      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
  }

  /** Operation - is transaction of money between users. */
//  public Future<Object> deleteOperation (PgPool dbClient, int id) {
//    return SqlTemplate
//      .forUpdate(dbClient, SQL_DELETE_OPERATION)
//      .execute(Collections.singletonMap("id", id))
//      .flatMap(rowSet -> {
//        if (rowSet.rowCount() > 0) {
//          return Future.succeededFuture();
//        } else {
//          throw new NoSuchElementException();
//        }
//      })
//      .onSuccess(res -> {
//        System.out.println("Delete operation is successful");
//      })
//      .onFailure(res -> {
//        System.out.println("Delete operation failed: " + res.getMessage());
//      });
//  }

  /** For deleteOperation(). To check before deleting operation (operation - transaction of money between users).
   *  Both receiver and sender should delete operation in order to delete it from database. */
  public Future<Object> updateSenderStatus (PgPool dbClient, int id) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE_SENDER_STATUS)
      .execute(Collections.singletonMap("id", id))
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          throw new NoSuchElementException();
        }
      })
      .onSuccess(res -> {
        System.out.println("Sender status update is successful");
      })
      .onFailure(res -> {
        System.out.println("Sender status update failed: " + res.getMessage());
      });
  }

  /** For deleteOperation(). To check before deleting operation (operation - transaction of money between users).
   *  Both receiver and sender should delete operation in order to delete it from database. */
  public Future<Object> updateReceiverStatus (PgPool dbClient, int id) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE_RECEIVER_STATUS)
      .execute(Collections.singletonMap("id", id))
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          throw new NoSuchElementException();
        }
      })
      .onSuccess(res -> {
        System.out.println("Receiver status update is successful");
      })
      .onFailure(res -> {
        System.out.println("Receiver status update failed: " + res.getMessage());
      });
  }

}



/** some deleted code FOR insertOperation through maps*/
// in the beginning
//    JsonObject object = new JsonObject()
//      .put("receiver_login", operation.getReceiver_login())
//      .put("sender_login", operation.getSender_login())
//      .put("money_amount", operation.getMoney_amount())
//      .put("date_id", operation.getDate_id())
//      .put("is_operation", true);

//inside of flatmap
//            if (res.iterator().hasNext()) {
//              int expense_id = res.iterator().next().getInteger("id");
//            }
//            if (res2.iterator().hasNext()) {
//              int income_id = res2.iterator().next().getInteger("id");
//            }
//            JsonObject operationInput = new JsonObject()
//              .put("expense_id", res.iterator().next().getInteger("id"))
//              .put("income_id", res2.iterator().next().getInteger("id"));
