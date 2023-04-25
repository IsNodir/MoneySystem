package com.example.MoneySystem;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

  public static final String IMAGE_VERSION = "postgres:15.2-alpine";
  public static final String DATABASE_NAME = "moneysystem2";
  public static PostgreSQLContainer container;

  public PostgresTestContainer()  {
    super(IMAGE_VERSION);
  }

  public static PostgreSQLContainer getInstance() {
    if(container == null) {
      container = new PostgresTestContainer().withDatabaseName(DATABASE_NAME).withUsername("postgres")
        .withPassword("db265");
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  public void stop() {
  }
}
