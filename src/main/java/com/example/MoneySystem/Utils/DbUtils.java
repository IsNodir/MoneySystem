package com.example.MoneySystem.Utils;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class DbUtils {

  private static final String HOST_CONFIG = "datasource.host";
  private static final String PORT_CONFIG = "datasource.port";
  private static final String DATABASE_CONFIG = "datasource.database";
  private static final String USERNAME_CONFIG = "datasource.username";
  private static final String PASSWORD_CONFIG = "datasource.password";

  private DbUtils() {
  }

   /**
   * Build DB client that is used to manage a pool of connections
   */
  public static PgPool buildDbClient(Vertx vertx) {

    final Properties properties = configureProperties();

//    PgConnectOptions connectOptions = new PgConnectOptions()
//      .setPort(Integer.parseInt(properties.getProperty(PORT_CONFIG)))
//      .setHost(properties.getProperty(HOST_CONFIG))
//      .setDatabase(properties.getProperty(DATABASE_CONFIG))
//      .setUser(properties.getProperty(USERNAME_CONFIG))
//      .setPassword(properties.getProperty(PASSWORD_CONFIG));

    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(Integer.parseInt(System.getProperty("DB_PORT")))
      .setHost(System.getProperty("DB_HOST"))
      .setDatabase(System.getProperty("DB_DATABASE"))
      .setUser(System.getProperty("DB_USERNAME"))
      .setPassword(System.getProperty("DB_PASSWORD"));

    final PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    return PgPool.pool(vertx, connectOptions, poolOptions);
  }

  private static Properties configureProperties() {

    final Properties properties = new Properties();

    final InputStream inputStream = DbUtils.class.getClassLoader().getResourceAsStream("application.properties");
    //application.properties

    try {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      inputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return properties;
  }

}
