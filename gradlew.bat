@rem Gradle wrapper script para Windows
@rem Uso: gradlew.bat build

@echo off
set APP_HOME=%~dp0
set GRADLE_OPTS=%GRADLE_OPTS% -Dfile.encoding=UTF-8

java %GRADLE_OPTS% ^
  -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" ^
  org.gradle.wrapper.GradleWrapperMain %*
