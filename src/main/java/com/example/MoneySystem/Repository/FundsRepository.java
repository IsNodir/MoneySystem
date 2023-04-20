package com.example.MoneySystem.Repository;

import com.example.MoneySystem.Model.DateDTO;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.sql.Date;

public class FundsRepository {

  private static final String SQL_SELECT_BALANCES_BY_ID_USER_DATE = "SELECT balance, date FROM public.funds WHERE id_user = $1 AND date BETWEEN $2 AND $3 ORDER BY date";
  private static final String SQL_SELECT_CURRENT_BALANCE = "SELECT balance FROM public.funds WHERE id_user = $1 "
    + "AND date = (SELECT MAX(date) as max_date FROM public.funds WHERE id_user = $1)";
  private static final String SQL_CHECK_BALANCE = "SELECT balance FROM public.funds WHERE id_user = $1 AND date = $2";
  protected static final String SQL_INSERT_BALANCE = "INSERT INTO public.funds (id_user, date, balance) " +
    "VALUES ($1, $2, $3) RETURNING balance";
  protected static final String SQL_UPDATE_BALANCE = "UPDATE public.funds SET balance = (balance + $1) WHERE id_user = $2 AND date = $3";

  public Future<RowSet<Row>> selectCurrentBalance(PgPool dbClient, int id) {
    return dbClient
      .preparedQuery(SQL_SELECT_CURRENT_BALANCE)
      .execute(Tuple.of(id))
//      .onSuccess(close -> { dbClient.close(); })
      .onFailure(error -> {System.out.println("Current balance selection failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> selectBalances(PgPool dbClient, DateDTO dateDTO) {
    return dbClient
      .preparedQuery(SQL_SELECT_BALANCES_BY_ID_USER_DATE)
      .execute(Tuple.of(dateDTO.getIdUser(), dateDTO.getDayFrom().toLocalDate(), dateDTO.getDayTo().toLocalDate()))
      .onFailure(error -> {System.out.println("Balances selection failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> insertBalance(PgPool dbClient, int idUser, Date date, double balance) {
    return dbClient
      .preparedQuery(SQL_INSERT_BALANCE)
      .execute(Tuple.of(idUser, date.toLocalDate(), balance))
      .onFailure(error -> {System.out.println("Balance insertion failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> checkBalance(PgPool dbClient, Date date, int idUser) {
    return dbClient
      .preparedQuery(SQL_CHECK_BALANCE)
      .execute(Tuple.of(idUser, date.toLocalDate()))
      .onFailure(error -> {System.out.println("Balance check failed: " + error.getMessage());});
  }
}





/** Query to select whole DB */

//  public Future<RowSet<Row>> selectWholeDbTest(PgPool dbClient) {
//    return dbClient
//      .query("SELECT * FROM public.users u INNER JOIN public.funds f ON u.id = f.id_user INNER JOIN public.operations o ON u.id = o.id_user INNER JOIN public.transactions t ON o.id = t.expense_id")
//      .execute();
//      //.onFailure(error -> {System.out.println("Current balance selection failed: " + error.getMessage());});
//  }
