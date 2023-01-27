package com.example.MoneySystem.Service;

import com.example.MoneySystem.Model.User;
import com.example.MoneySystem.Repository.UserRepository;
import io.vertx.pgclient.PgPool;

public class UserService {
  private final PgPool dbClient;
  private final UserRepository userRepository;

  public UserService(PgPool dbClient, UserRepository userRepository) {
    this.dbClient = dbClient;
    this.userRepository = userRepository;
  }

  public void updatePassword(User user) {
    dbClient.withTransaction(
      connection -> {
        userRepository.updatePassword(connection, user);
        return null;
      });
  }
}
