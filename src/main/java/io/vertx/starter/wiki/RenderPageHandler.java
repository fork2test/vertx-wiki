package io.vertx.starter.wiki;

import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by qikhan on 7/19/2017.
 */
public class RenderPageHandler {

  private final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();

  private static final String SQL_GET_PAGE = "select Id, Content from Pages where Name = ?";
  private static final String EMPTY_PAGE_MARKDOWN =
    "# A new page\n" +
      "\n" +
      "Feel-free to write in Markdown!\n";

  public void process(RoutingContext context, JDBCClient dbClient) {

    String page = context.request().getParam("page");
    if (page == null || page.trim().isEmpty()) {
      context.fail(new Exception("page is empty"));
      return;
    }
    dbClient.getConnection(db -> {
      if (db.succeeded()) {
        SQLConnection conn = db.result();

        conn.queryWithParams(SQL_GET_PAGE
          , new JsonArray().add(page)
          , res -> {
            conn.close();

            if (res.succeeded()) {

              JsonArray row = res.result()
                .getResults()
                .stream()
                .findFirst().orElseGet(() -> new JsonArray().add(-1).add(EMPTY_PAGE_MARKDOWN));

              Integer id = row.getInteger(0);
              String rawContent = row.getString(1);

              context.put("title", page);
              context.put("id", id);
              context.put("newPage", res.result().getResults().size() == 0 ? "yes" : "no");
              context.put("rawContent", rawContent);
              context.put("content", Processor.process(rawContent));
              context.put("timestamp", new Date().toString());

              templateEngine.render(context, "templates/page.ftl", ar -> {
                if (ar.succeeded()) {
                  context.response().putHeader("Content-Type", "text/html");
                  context.response().end(ar.result());
                } else {
                  context.fail(ar.cause());
                }
              });

            } else {
              context.failed();
            }
          });
      } else {
        context.failed();
      }
    });
  }
}
