package io.vertx.starter.app;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.starter.wiki.*;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
  public static final int HTTP_PORT = 8080;

  private JDBCClient dbClient;
  private IndexPageHandler indexPageHandler = new IndexPageHandler();
  private RenderPageHandler renderPageHandler = new RenderPageHandler();
  private UpdatePageHandler updatePageHandler = new UpdatePageHandler();
  private DeletePageHandler deletePageHandler = new DeletePageHandler();
  private TimerHandler timerHandler = new TimerHandler();

  private static final String LOGGER_DELEGATE_FACTORY_CLASS_NAME_PROPERTY = "vertx.logger-delegate-factory-class-name";
  private static final String LOGGER_DELEGATE_FACTORY_CLASS_NAME = "io.vertx.core.logging.SLF4JLogDelegateFactory";


  @Override
  public void init(Vertx vertx, Context context) {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME_PROPERTY, LOGGER_DELEGATE_FACTORY_CLASS_NAME);
    VertxOptions options = new VertxOptions().setMetricsOptions(
      new DropwizardMetricsOptions().setEnabled(true).setRegistryName("my-registry")
    );
    this.vertx = Vertx.vertx(options);
    this.context = context;
  }

  @Override
  public void start(Future<Void> startFuture) {
    logger.info("metrics:" + vertx.isMetricsEnabled());
    MetricsService service = MetricsService.create(vertx);
    service.metricsNames().forEach(name -> {
      logger.info(name);
    });
    vertx.setPeriodic(5000, t -> {
      JsonObject metrics = service.getMetricsSnapshot("vertx.pools.datasource");
      logger.info(metrics);

    });
    vertx.setPeriodic(5000, t -> {
      logger.info("ciao ciao");
      logger.info("ciao ciao logger");
    });

    Future<Void> steps = initDatabase().compose(v -> initHttpServer());
    steps.setHandler(startFuture.completer());
  }


  private Future<Void> initDatabase() {
    Future<Void> future = Future.future();

    DatabaseClientBuilder.build(vertx).setHandler(dbClientFuture -> {
      if (dbClientFuture.failed()) {
        future.fail(dbClientFuture.cause());
      } else {
        dbClient = dbClientFuture.result();
        future.complete();
        logger.info("DB client creation completed.");
      }
    });
    return future;
  }

  private Future<Void> initHttpServer() {
    Future<Void> future = Future.future();
    HttpServer server = vertx.createHttpServer();
    logger.info("Initializing http server and app router");
    Router router = buildRouter();

    server
      .requestHandler(router::accept)
      .listen(HTTP_PORT, ar -> {
        if (ar.succeeded()) {
          logger.info("HTTP server running on port " + HTTP_PORT);
          future.complete();

        } else {
          logger.error("Could not start a HTTP server", ar.cause());
          future.fail(ar.cause());
        }
      });
    return future;
  }

  private Router buildRouter() {

    Router router = Router.router(vertx);
    router.get("/").handler(this::indexHandler);
    router.get("/wiki/:page").handler(this::pageRenderingHandler);
    router.post().handler(BodyHandler.create());
    router.post("/save").handler(this::pageUpdateHandler);
    router.post("/create").handler(this::pageCreateHandler);
    router.post("/delete").handler(this::pageDeletionHandler);
    router.get("/start").handler(this::startTimer);
    router.get("/stop").handler(this::stopTimer);

    return router;
  }

  private void pageDeletionHandler(RoutingContext context) {
    deletePageHandler.process(context, dbClient);
    //    context.response().setStatusCode(200);
//    context.response().end();
  }

  private void pageCreateHandler(RoutingContext context) {
    String pageName = context.request().getParam("name");
    logger.info("pageCreateHandler name: " + pageName);
    String location = "/wiki/" + pageName;
    if (pageName == null || pageName.isEmpty()) {
      location = "/";
    }
    context.response().setStatusCode(303);
    context.response().putHeader("Location", location);
    context.response().end();
  }

  private void pageUpdateHandler(RoutingContext context) {
    logger.info("pageUpdateHandler");
    updatePageHandler.process(context, dbClient);

  }

  private void pageRenderingHandler(RoutingContext context) {
    logger.info("pageRenderingHandler");
    renderPageHandler.process(context, dbClient);
  }

  private void indexHandler(RoutingContext context) {
    logger.info("indexHandler");
    indexPageHandler.process(context, dbClient);
  }

  private void startTimer(RoutingContext context) {
    logger.info("startTimer");
    SharedData sd = vertx.sharedData();
    LocalMap<String, Long> localMap = sd.getLocalMap("timer_local");
    timerHandler.start(context, localMap, vertx);
  }

  private void stopTimer(RoutingContext context) {
    logger.info("stopTimer");
    SharedData sd = vertx.sharedData();
    LocalMap<String, Long> localMap = sd.getLocalMap("timer_local");
    timerHandler.stop(context, localMap, vertx);
  }
}
