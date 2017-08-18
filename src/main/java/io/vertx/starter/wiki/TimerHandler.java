package io.vertx.starter.wiki;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by qikhan on 7/19/2017.
 */
public class TimerHandler {

  private static final Logger logger = LoggerFactory.getLogger(TimerHandler.class);


  public void start(RoutingContext context, LocalMap<String, Long> localmap, Vertx vertx) {
    if (localmap.containsKey("timer_id")) {
      context.response().putHeader("Content-Type", "text/html");
      context.response().end("<html><body>timer already exist</body></html>");
    } else {
      long timer_id = vertx.setPeriodic(5000, timer -> {
        logger.info("timer ok!");
      });
      localmap.put("timer_id", timer_id);
      context.response().putHeader("Content-Type", "text/html");
      context.response().end("<html><body>timer started</body></html>");
    }
  }

  public void stop(RoutingContext context, LocalMap<String, Long> localmap, Vertx vertx) {
    if (localmap.containsKey("timer_id")) {
      long timer_id = localmap.get("timer_id");
      boolean result = vertx.cancelTimer(timer_id);
      if (result) {
        localmap.remove("timer_id");
        context.response().putHeader("Content-Type", "text/html");
        context.response().end("<html><body>timer cancelled</body></html>");
      } else {
        context.response().putHeader("Content-Type", "text/html");
        context.response().end("<html><body>impossibile to delete the timer</body></html>");
      }
    } else {
      context.response().putHeader("Content-Type", "text/html");
      context.response().end("<html><body>timer not started</body></html>");
    }

  }
}
