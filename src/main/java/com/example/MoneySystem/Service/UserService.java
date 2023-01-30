package com.example.MoneySystem.Service;

import com.example.MoneySystem.Model.User;
import com.example.MoneySystem.Repository.UserRepository;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.templates.SqlTemplate;

public class UserService {

  private static final String SQL_UPDATE = "UPDATE users SET password = #{password}, WHERE login = #{login}";
  private final PgPool dbClient;

  private final SqlClient client;

  private final UserRepository userRepository;

  public UserService(PgPool dbClient, UserRepository userRepository) {
    this.dbClient = dbClient;
    this.client = dbClient;
    this.userRepository = userRepository;
  }

  public void updatePassword(User user) {

    dbClient.withTransaction(
      connection -> {
        return userRepository.updatePassword(connection, user);
//        return null;
      })
      .onSuccess(v -> System.out.println("Transaction succeeded"))
      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));

//    SqlTemplate
//      .forUpdate(dbClient, SQL_UPDATE)
//      .mapFrom(User.class)
//      .execute(user)
////      .flatMap(rowSet -> {
////        if (rowSet.rowCount() > 0) {
////          return Future.succeededFuture(user);
////        } else {
////          throw new NoSuchElementException(user.getLogin());
////        }
////      })
//      .onSuccess(res -> {
//        System.out.println("User updated");
//      })
//      .onFailure(res -> {
//        System.out.println("User NOT updated");
//      });
  }

  public void createUser(User user) {
    String login = user.getLogin();
    String password = user.getPassword();
    client
      .preparedQuery("INSERT INTO user (login, password) VALUES ($1, $2)")
      .execute(Tuple.of(login, password), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          System.out.println(rows.rowCount());
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }
}
