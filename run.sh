#!/usr/bin/env bash

export LAUNCHER="io.vertx.core.Launcher"
export VERTICLE="io.vertx.starter.app.MainVerticle"
export VERTX_CMD="run"
export VERTX_OPTS="-Dvertx.metrics.options.enabled=true"
JAVA_OPTS="-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"

java $JAVA_OPTS \
  -cp  "$(echo target/dependency/*.jar | tr ' ' ':'):target/vertx-wiki-1.0-SNAPSHOT.jar" \
  $LAUNCHER $VERTX_CMD $VERTICLE
  $@
