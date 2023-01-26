package com.example.MoneySystem.Repository;

import com.example.MoneySystem.Model.User;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.NoSuchElementException;

public class UserRepository {

  private static final String SQL_UPDATE = "UPDATE user SET password = #{password}, WHERE login = #{login}";

  public UserRepository () {
  }

  public Future<User> updatePassword(SqlConnection connection,
                             User user) {
    return SqlTemplate
      .forUpdate(connection, SQL_UPDATE)
      .mapFrom(User.class)
      .execute(user)
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture(user);
        } else {
          throw new NoSuchElementException(user.getLogin());
        }
      })
      .onSuccess(success -> System.out.println("Password updated successfully"))
      .onFailure(throwable -> System.out.println("Password NOT updated - Error"));
  }
}
