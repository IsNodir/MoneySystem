package com.example.MoneySystem.Repository;

import com.example.MoneySystem.Model.OperationDTO;
import com.example.MoneySystem.Model.UsersDTO;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.NoSuchElementException;

public class UsersRepository extends FundsRepository{

  private static final String SQL_UPDATE = "UPDATE public.users SET password = #{new_password} WHERE login = #{login} AND password = #{password}";
  private static final String SQL_INSERT = "INSERT INTO public.users VALUES (#{login},#{password})";
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM public.users WHERE login = #{login}";
  private static final String SQL_INSERT_USER = "INSERT INTO public.users (login, password) VALUES ($1, $2) RETURNING id";

  public UsersRepository() {
  }

  /**
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

  public Future<RowSet<Row>> insertUser(PgPool dbClient, UsersDTO usersDTO, LocalDate date) {
    return dbClient.withTransaction(client -> client
        .preparedQuery(SQL_INSERT_USER)
        .execute(Tuple.of(usersDTO.getLogin(), usersDTO.getPassword()))
        .flatMap(res -> client
          .preparedQuery(SQL_INSERT_BALANCE)
          .execute(Tuple.of(res.iterator().next().getInteger("id"), date, 0))))
      .onFailure(error -> {System.out.println("Operation failed: " + error.getMessage());});
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
  }
}
