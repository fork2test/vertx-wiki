FROM openjdk:8-jre-alpine

ENV VERTICLE_FILE vertx-wiki-1.0-SNAPSHOT.jar
ENV VERTICLE_HOME /usr/verticles
ENV VERTICLE_LIB /usr/verticles/lib

EXPOSE 8080

COPY target/$VERTICLE_FILE $VERTICLE_LIB/
COPY target/dependency/* $VERTICLE_LIB/
ENV LAUNCHER="io.vertx.core.Launcher"
ENV VERTICLE="io.vertx.starter.app.MainVerticle"
ENV VERTX_OPTS="-Dvertx.metrics.options.enabled=true"
ENV VERTX_CMD="run"

ENV JAVA_OPTS="-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"


# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java $JAVA_OPTS -cp $(echo $VERTICLE_LIB/*.jar | tr ' ' ':') $LAUNCHER $VERTX_CMD $VERTICLE"]
