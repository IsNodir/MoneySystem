package com.example.MoneySystem.Repository;

import com.example.MoneySystem.Model.UsersDTO;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Collections;
import java.util.NoSuchElementException;

public class UsersRepository {

  private static final String SQL_UPDATE = "UPDATE public.users SET password = #{password} WHERE login = #{login}";
  private static final String SQL_INSERT = "INSERT INTO public.users VALUES (#{login},#{password})";
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM public.users WHERE login = #{login}";

  public UsersRepository() {
  }

  /*
   Future <Object> in this case it returns result of Query to database
   So we can access through other classes which calls this method "onSuccess()" and "onFailure()" methods
   or any other methods which gives result of query to database (successful or not)
   Difference from Future<SqlResult<Void>> is that flatMap() method doesn't work properly there
  */
  public Future<Object> updatePassword(PgPool dbClient,
                                      UsersDTO user) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE)
      .mapFrom(UsersDTO.class)
      .execute(user)
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          return Future.failedFuture(new NoSuchElementException());
        }
      })
      .onSuccess(res -> {
        System.out.println("User updated successfully");
      })
      .onFailure(res -> {
        System.out.println("User NOT updated: " + res.getMessage());
      });
  }

  public Future<SqlResult<Void>> insert(PgPool dbClient,
                                        UsersDTO user) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_INSERT)
      .mapFrom(UsersDTO.class)
      .execute(user)
      .onSuccess(res -> {
        System.out.println("User inserted successfully");
      })
      .onFailure(res -> {
        System.out.println("User NOT inserted: " + res.getMessage());
      });
  }

  // To return password. Used in service class for checking whether user entered the same password as before
  public Future<UsersDTO> selectByLogin (PgPool dbClient, String login) {
    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_BY_ID)
      .mapTo(UsersDTO.class)
      .execute(Collections.singletonMap("login", login))
      .map(rowSet -> {
        final RowIterator<UsersDTO> iterator = rowSet.iterator();

        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException(login);
        }
      })
      .onFailure(error -> System.out.println("User NOT found: " + error.getMessage()));

      //    dbClient
      //      .query(SQL_SELECT_BY_ID)
      //      .execute(ar -> {
      //        if (ar.succeeded()) {
      //          UsersDTO user = (UsersDTO) ar.result();
      //          System.out.println("Got " + user.getLogin());
      //        } else {
      //          System.out.println("Failure: " + ar.cause().getMessage());
      //        }
      //      });
  }
}
