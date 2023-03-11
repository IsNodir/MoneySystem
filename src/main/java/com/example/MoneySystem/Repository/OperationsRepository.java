package com.example.MoneySystem.Repository;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Model.OperationDTO;
import com.example.MoneySystem.Model.TransactionDTO;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.sql.Date;

public class OperationsRepository extends FundsRepository{

  private static final String SQL_UPDATE_SENDER_DELETE_STATUS = "UPDATE public.transactions SET sender_delete = true WHERE expense_id = $1 RETURNING receiver_delete, expense_id, income_id";
  private static final String SQL_UPDATE_RECEIVER_DELETE_STATUS = "UPDATE public.transactions SET receiver_delete = true WHERE income_id = $1 RETURNING sender_delete, expense_id, income_id";
  private static final String SQL_SELECT_TRANSACTION_BY_INCOME_ID = "SELECT expense_id, income_id, sender_delete, receiver_delete FROM public.transactions WHERE income_id = $1";
  private static final String SQL_SELECT_TRANSACTION_BY_EXPENSE_ID = "SELECT expense_id, income_id, sender_delete, receiver_delete FROM public.transactions WHERE expense_id = $1";
  private static final String SQL_INSERT_OPERATION = "INSERT INTO public.operations (id_user, amount, date, is_operation, is_expense) " +
    "VALUES ($1, $2, $3, $4, $5) RETURNING id";
  private static final String SQL_DELETE_OPERATION = "DELETE FROM public.operations WHERE id = $1 RETURNING is_expense, id_user, date, amount";
  private static final String SQL_SELECT_OPERATIONS_BY_DATE = "SELECT id, amount, date, is_expense FROM public.operations WHERE id_user = $1 AND date BETWEEN $2 AND $3";
  private static final String SQL_INSERT_TRANSACTION = "INSERT INTO public.transactions (expense_id, income_id) " +
    "VALUES ($1, $2) RETURNING id";
  private static final String SQL_SELECT_IS_OPERATION_IS_EXPENSE = "SELECT is_operation, is_expense FROM public.operations WHERE id = $1";
  private static final String SQL_SELECT_USERID_BY_OPERATIONID = "SELECT id_user FROM public.operations WHERE id = $1 OR id = $2 AND id_user != $3";

  public Future<RowSet<Row>> insertIncomeOperation(PgPool dbClient, OperationDTO operationDTO) {
    return dbClient.withTransaction(client -> client
        .preparedQuery(SQL_INSERT_OPERATION)
        .execute(Tuple.of(operationDTO.getIdUser(), operationDTO.getAmount(), operationDTO.getDate().toLocalDate(),
          operationDTO.isOperation(), operationDTO.isExpense()))
        .flatMap(res -> client
          .preparedQuery(SQL_UPDATE_BALANCE)
          .execute(Tuple.of(operationDTO.getAmount(), operationDTO.getIdUser(), operationDTO.getDate().toLocalDate()))))
      .onFailure(error -> {System.out.println("Operation failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> insertExpenseOperation(PgPool dbClient, OperationDTO operationDTO) {
    return dbClient.withTransaction(client -> client
        .preparedQuery(SQL_INSERT_OPERATION)
        .execute(Tuple.of(operationDTO.getIdUser(), operationDTO.getAmount(), operationDTO.getDate().toLocalDate(),
          operationDTO.isOperation(), operationDTO.isExpense()))
        .flatMap(res -> client
          .preparedQuery(SQL_UPDATE_BALANCE)
          .execute(Tuple.of(-operationDTO.getAmount(), operationDTO.getIdUser(), operationDTO.getDate().toLocalDate()))))
      .onFailure(error -> {System.out.println("Operation failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> deleteOperation(PgPool dbClient, int id) {
    return dbClient.withTransaction(client -> client
      .preparedQuery(SQL_DELETE_OPERATION)
      .execute(Tuple.of(id))
      .flatMap(delete -> client
        .preparedQuery(SQL_UPDATE_BALANCE)
        .execute(Tuple.of(delete.iterator().next().getDouble("amount"),
          delete.iterator().next().getInteger("id_user"), delete.iterator().next().getLocalDate("date")))
      ))
      .onFailure(error -> {System.out.println("Operation delete failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> selectOperationsByDate(PgPool dbClient, DateDTO dateDTO) {
    return dbClient
      .preparedQuery(SQL_SELECT_OPERATIONS_BY_DATE)
      .execute(Tuple.of(dateDTO.getIdUser(), dateDTO.getDayFrom().toLocalDate(), dateDTO.getDayTo().toLocalDate()))
      .onFailure(error -> {System.out.println("Operation selection failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> selectTransactionByIncomeId(PgPool dbClient, int income_id) {
    return dbClient
      .preparedQuery(SQL_SELECT_TRANSACTION_BY_INCOME_ID)
      .execute(Tuple.of(income_id))
      .onFailure(error -> {System.out.println("Transaction selection failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> selectTransactionByExpenseId(PgPool dbClient, int expense_id) {
    return dbClient
      .preparedQuery(SQL_SELECT_TRANSACTION_BY_EXPENSE_ID)
      .execute(Tuple.of(expense_id))
      .onFailure(error -> {System.out.println("Transaction selection failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> insertTransaction(PgPool dbClient, TransactionDTO transactionDTO) {
    return dbClient.withTransaction(client -> client
        .preparedQuery(SQL_INSERT_OPERATION)
        .execute(Tuple.of(transactionDTO.getIdSender(), transactionDTO.getAmount(), transactionDTO.getDate().toLocalDate(),
          true, true))
        .flatMap(res -> client
          .preparedQuery(SQL_UPDATE_BALANCE)
          .execute(Tuple.of(-transactionDTO.getAmount(), transactionDTO.getIdSender(), transactionDTO.getDate().toLocalDate()))
          .flatMap(res2 -> client
            .preparedQuery(SQL_INSERT_OPERATION)
            .execute(Tuple.of(transactionDTO.getIdReceiver(), transactionDTO.getAmount(), transactionDTO.getDate().toLocalDate(),
              true, false))
            .flatMap(res3 -> client
              .preparedQuery(SQL_UPDATE_BALANCE)
              .execute(Tuple.of(transactionDTO.getAmount(), transactionDTO.getIdReceiver(), transactionDTO.getDate().toLocalDate()))
              .flatMap(res4 -> client
                .preparedQuery(SQL_INSERT_TRANSACTION)
                .execute(Tuple.of(res.iterator().next().getInteger("id"), res3.iterator().next().getInteger("id")))
          )))))
      .onSuccess(v -> System.out.println("Transaction succeeded"))
      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
  }

  public Future<String> deleteTransaction(PgPool dbClient, int expense_id, int income_id, Date currentDate) {
    return dbClient.withTransaction(client -> client
        .preparedQuery(SQL_DELETE_OPERATION)
        .execute(Tuple.of(expense_id))
        .flatMap(expenseDelete -> client
          .preparedQuery(SQL_DELETE_OPERATION)
          .execute(Tuple.of(income_id))
          .flatMap(incomeDelete -> client
            .preparedQuery(SQL_UPDATE_BALANCE)
            .execute(Tuple.of(expenseDelete.iterator().next().getDouble("amount"),
              expenseDelete.iterator().next().getInteger("id_user"), currentDate.toLocalDate()))
            .flatMap(expenseBalance -> client
              .preparedQuery(SQL_UPDATE_BALANCE)
              .execute(Tuple.of(-incomeDelete.iterator().next().getDouble("amount"),
                incomeDelete.iterator().next().getInteger("id_user"), currentDate.toLocalDate()))
              .map("Operation deleted")))));
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

  public Future<RowSet<Row>> selectIsOperationIsExpense(PgPool dbClient, int id) {
    return dbClient
      .preparedQuery(SQL_SELECT_IS_OPERATION_IS_EXPENSE)
      .execute(Tuple.of(id))
      .onFailure(error -> {System.out.println("is_expense selection failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> selectUserIdByOperationId (PgPool dbClient, int idOperation, int idAnotherOperation, int anotherUserId) {
    return dbClient
      .preparedQuery(SQL_SELECT_USERID_BY_OPERATIONID)
      .execute(Tuple.of(idOperation, idAnotherOperation, anotherUserId))
      .onFailure(error -> {System.out.println("userId selection failed: " + error.getMessage());});
  }

}
