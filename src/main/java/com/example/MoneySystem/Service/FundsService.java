package com.example.MoneySystem.Service;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Model.FundDTO;
import com.example.MoneySystem.Repository.FundsRepository;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class FundsService {

  private final PgPool dbClient;

  private final FundsRepository fundsRepository = new FundsRepository();

  public FundsService(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  public void selectBalances(DateDTO dateDTO, RoutingContext ctx) {
    fundsRepository.selectBalances(dbClient, dateDTO)
      .onSuccess(res -> {
        List<FundDTO> fundsList = new ArrayList<>();

        res.forEach(each -> {
          Date date = Date.valueOf(each.getLocalDate("date"));
          double balance = each.getDouble("balance");

          fundsList.add(new FundDTO(balance, date));
        });

        ctx.request().response().end(String.format(fundsList.toString()));
      })
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Balance NOT found: " + error.getMessage()));});

  }

  public void selectCurrentBalance(int id, RoutingContext ctx) {
    fundsRepository.selectCurrentBalance(dbClient, id)
      .onSuccess(res -> {ctx.request().response().end(String.format("Balance: " + res.iterator().next().getDouble("balance")));})
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("Balance NOT found: " + error.getMessage()));});
  }
}
