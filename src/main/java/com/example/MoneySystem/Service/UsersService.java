package com.example.MoneySystem.Service;

import com.example.MoneySystem.Model.*;
import com.example.MoneySystem.Repository.UsersRepository;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;

public class UsersService {
  private final PgPool dbClient;

  private final UsersRepository usersRepository = new UsersRepository();

  public UsersService(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  public Future<Object> updatePassword(UsersDTO users) {
    return usersRepository.updatePassword(dbClient, users);
  }

  public Future<SqlResult<Void>> createUser(UsersDTO user) {
    return usersRepository.insert(dbClient, user);
  }

  public Future<UsersDTO> searchByLogin(UsersDTO users) {
    return usersRepository.selectByLogin(dbClient, users.getLogin());
  }
}
