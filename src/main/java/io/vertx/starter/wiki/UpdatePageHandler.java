package io.vertx.starter.wiki;

import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.util.Date;

/**
 * Created by qikhan on 7/19/2017.
 */
public class UpdatePageHandler {

  private static final String SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)";
  private static final String SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?";

  public void process(RoutingContext context, JDBCClient dbClient) {

    HttpServerRequest request = context.request();
    String id = request.getParam("id");
    String title = request.getParam("title");
    String markdown = request.getParam("markdown");
    boolean newPage = "yes".equals(request.getParam("newPage"));

    dbClient.getConnection(car -> {

      if (car.succeeded()) {

        SQLConnection connection = car.result();
        String sql = newPage ? SQL_CREATE_PAGE : SQL_SAVE_PAGE;

        JsonArray params = new JsonArray();

        if (newPage) {
          params.add(title).add(markdown);
        } else {
          params.add(markdown).add(id);
        }

        connection.updateWithParams(sql, params, res -> {
          connection.close();
          if (res.succeeded()) {
            context.response().setStatusCode(303);
            context.response().putHeader("Location", "/wiki/" + title);
            context.response().end();
          } else {
            context.fail(res.cause());
          }
        });
      } else {
        context.fail(car.cause());
      }
    });
  }
}
