package io.vertx.starter.wiki;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;


/**
 * Created by qikhan on 7/19/2017.
 */
public class DeletePageHandler {


  public static final String SQL_DELETE_PAGE = "delete from Pages where Id = ?";

  public void process(RoutingContext context, JDBCClient dbClient) {

    String id = context.request().getParam("id");

    dbClient.getConnection(db -> {

      if (db.succeeded()) {
        SQLConnection conn = db.result();

        conn.updateWithParams(SQL_DELETE_PAGE
          , new JsonArray().add(id)
          , res -> {
            conn.close();

            if (res.succeeded()) {
              context.response().setStatusCode(303);
              context.response().putHeader("Location", "/");
              context.response().end();
            } else {
              context.fail(res.cause());
            }
        });
      } else {
        context.failed();
      }
    });
  }
}
