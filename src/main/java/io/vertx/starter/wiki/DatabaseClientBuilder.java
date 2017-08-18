package io.vertx.starter.wiki;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;


/**
 * Created by qikhan on 7/13/2017.
 */
public class DatabaseClientBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseClientBuilder.class);

  private static final String SQL_CREATE_PAGES_TABLE = "create table " +
    " if not exists Pages " +
    " ( " +
    "  Id integer identity primary key " +
    ", Name varchar(255) unique " +
    ", Content clob " +
    ")";

  public static Future<JDBCClient> build(Vertx vertx) {

    Future<JDBCClient> future = Future.future();

    JDBCClient jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:hsqldb:file:db/wiki")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30));

    LOGGER.info("Initializing the JDBC connection");

    jdbcClient.getConnection(ar -> {
      if (ar.failed()) {
        LOGGER.error("Could not open a database connection", ar.cause());
        future.fail(ar.cause());
      } else {
        SQLConnection connection = ar.result();
        connection.execute(SQL_CREATE_PAGES_TABLE, create -> {
          connection.close();
          if (create.failed()) {
            LOGGER.error("Database preparation error", create.cause());
            future.fail(create.cause());
          }
          else {
            future.complete(jdbcClient);
            LOGGER.info("JDBC connection successful");
          }
        });
      }
    });

    return future;
  }
}
