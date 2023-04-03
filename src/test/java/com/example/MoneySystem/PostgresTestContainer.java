package com.example.MoneySystem;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

  public static final String IMAGE_VERSION = "postgres:15.2-alpine";
  public static final String DATABASE_NAME = "moneysystem2";
  public static final int DATABASE_PORT = 5432;
  public static PostgreSQLContainer container;

  public PostgresTestContainer()  {
    super(IMAGE_VERSION);
  }

  public static PostgreSQLContainer getInstance() {
    if(container == null) {
      container = new PostgresTestContainer().withDatabaseName(DATABASE_NAME).withExposedPorts(DATABASE_PORT);
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    //container.get
    // System.setProperty("DB_URL", container.getJdbcUrl());
    System.setProperty("DB_HOST", container.getHost());
    System.setProperty("DB_DATABASE", container.getDatabaseName());
    //System.setProperty("DB_PORT", String.valueOf(container.getFirstMappedPort()));
    System.setProperty("DB_USERNAME", container.getUsername());
    System.setProperty("DB_PASSWORD", container.getPassword());
  }

  @Override
  public void stop() {
  }
}
