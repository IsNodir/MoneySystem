package com.example.MoneySystem.Utils;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class DbUtils {

  private static final String HOST_CONFIG = "localhost";
  private static final String PORT_CONFIG = "5432";
  private static final String DATABASE_CONFIG = "moneysystem";
  private static final String USERNAME_CONFIG = "postgres";
  private static final String PASSWORD_CONFIG = "db265";

  private DbUtils() {
  }

  /**
   * Build DB client that is used to manage a pool of connections
   *
   * @param vertx Vertx context
   * @return PostgreSQL pool
   */
  public static PgPool buildDbClient(Vertx vertx) {

    final PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(Integer.parseInt(PORT_CONFIG))
      .setHost(HOST_CONFIG)
      .setDatabase(DATABASE_CONFIG)
      .setUser(USERNAME_CONFIG)
      .setPassword(PASSWORD_CONFIG);

    final PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    return PgPool.pool(vertx, connectOptions, poolOptions);
  }

}
