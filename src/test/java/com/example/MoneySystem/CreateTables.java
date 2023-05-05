package com.example.MoneySystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTables {

  public static void create(String jdbcUrl) {
    String url = jdbcUrl;
    String user = "postgres";
    String password = "db265";

    String usersTable = """
       create table public.users (
       id       serial      primary key,
       login     varchar(50) not null constraint users_pk unique,
       password  varchar(50) not null)
       """;

    String fundsTable = """
      create table public.funds (
      id       serial           primary key,
      id_user  integer          not null  references public.users  on delete cascade,
      date     date             not null,
      balance  double precision not null)
      """;

    String operationsTable = """
      create table public.operations (
      id           serial                primary key,
      id_user      integer               not null  references public.users,
      amount       double precision      not null,
      date         date                  not null,
      is_operation boolean default false not null,
      is_expense   boolean               not null)
      """;

    String transactionsTable = """
      create table public.transactions (
      id              serial   primary key,
      expense_id      integer  not null  constraint fk_expense_id  references public.operations  on delete cascade,
      income_id       integer  not null  constraint fk_income_id   references public.operations,
      sender_delete   boolean default false not null,
      receiver_delete boolean default false not null)
      """;

    try (Connection conn = DriverManager.getConnection(url, user, password);
         Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(usersTable);
      stmt.executeUpdate(fundsTable);
      stmt.executeUpdate(operationsTable);
      stmt.executeUpdate(transactionsTable);
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
}
