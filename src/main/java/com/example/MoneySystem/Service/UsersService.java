package com.example.MoneySystem.Service;

import com.example.MoneySystem.Model.*;
import com.example.MoneySystem.Repository.UsersRepository;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;

import java.time.LocalDate;

public class UsersService {
  private final PgPool dbClient;

  private final UsersRepository usersRepository = new UsersRepository();

  public UsersService(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  public Future<Object> updatePassword(UsersDTO users) {
    return usersRepository.updatePassword(dbClient, users);
  }

  public void insertUser(UsersDTO usersDTO, RoutingContext ctx) {
    LocalDate date = LocalDate.now();
    usersRepository.insertUser(dbClient, usersDTO, date)
      .onSuccess(res -> {ctx.request().response().end(String.format("User created successfully"));})
      .onFailure(error -> {ctx.request().response().setStatusCode(400).end(String.format("User insertion failed: " + error.getMessage()));});
  }

  public Future<UsersDTO> searchByLogin(UsersDTO users) {
    return usersRepository.selectByLogin(dbClient, users.getLogin());
  }
}
