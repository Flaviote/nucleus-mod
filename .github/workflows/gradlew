#!/bin/sh
# Gradle wrapper script para Linux/Mac
# Uso: ./gradlew build

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
GRADLE_OPTS="${GRADLE_OPTS:-} -Dfile.encoding=UTF-8"

exec "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || \
java $GRADLE_OPTS \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
