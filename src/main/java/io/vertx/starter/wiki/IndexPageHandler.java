package io.vertx.starter.wiki;

import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by qikhan on 7/19/2017.
 */
public class IndexPageHandler {

  private final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();
  private static final String SQL_ALL_PAGES = "select Name from Pages";

  public void process(RoutingContext context, JDBCClient dbClient) {


    dbClient.getConnection(db -> {
      if (db.succeeded()) {
        SQLConnection conn = db.result();

        conn.query(SQL_ALL_PAGES, res -> {
          conn.close();

          if (res.succeeded()) {

            List<String> pages = res.result()
              .getResults()
              .stream()
              .map(json -> json.getString(0))
              .sorted()
              .collect(Collectors.toList());

            context.put("title", "Wiki home");
            context.put("pages", pages);
            templateEngine.render(context, "templates/index.ftl", ar -> {
              if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html");
                context.response().end(ar.result());
              } else {
                context.fail(ar.cause());
              }
            });

          }
          else {
            context.failed();
          }
        });
      } else {
        context.failed();
      }
    });
  }
}
