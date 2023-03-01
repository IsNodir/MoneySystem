package com.example.MoneySystem.Repository;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Model.FundDTO;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class FundsRepository {

  private static final String SQL_SELECT_BALANCES_BY_ID_USER_DATE = "SELECT balance, date FROM public.funds WHERE id_user = $1 AND date BETWEEN $2 AND $3";
  private static final String SQL_SELECT_CURRENT_BALANCE = "SELECT balance FROM public.funds WHERE id_user = $1 "
    + "AND date = (SELECT MAX(date) as max_date FROM public.funds WHERE id_user = $1)";
  private static final String SQL_UPDATE_BALANCE = "UPDATE public.funds SET balance = (balance + $1) WHERE id_user = $2 AND date = $3";

  public Future<RowSet<Row>> selectCurrentBalance(PgPool dbClient, int id) {
    return dbClient
      .preparedQuery(SQL_SELECT_CURRENT_BALANCE)
      .execute(Tuple.of(id))
      .onFailure(error -> {System.out.println("Current balance selection failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> selectBalances(PgPool dbClient, DateDTO dateDTO) {
    return dbClient
      .preparedQuery(SQL_SELECT_BALANCES_BY_ID_USER_DATE)
      .execute(Tuple.of(dateDTO.getIdUser(), dateDTO.getDayFrom().toLocalDate(), dateDTO.getDayTo().toLocalDate()))
      .onFailure(error -> {System.out.println("Balances selection failed: " + error.getMessage());});
  }

  public Future<RowSet<Row>> updateBalance(PgPool dbClient, FundDTO fundDTO) {
    return dbClient
      .preparedQuery(SQL_UPDATE_BALANCE)
      .execute(Tuple.of(fundDTO.getChangeInBalance(), fundDTO.getIdUser(), fundDTO.getDate()))
      .onFailure(error -> {System.out.println("Balance update failed: " + error.getMessage());});
  }


}
