#!/bin/sh
SERVICE_NAME=urouterService
PATH_TO_JAR=/opt/router/urouter/bin/${project.artifactId}-${project.version}.jar
PID_PATH_NAME=/var/run/urouter.pid
APPLICATION_PROPERTIES_FILE=/opt/router/urouter/config/application.properties

case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java -jar "$PATH_TO_JAR" --spring.config.location=file:///$APPLICATION_PROPERTIES_FILE /tmp 2>> /dev/null >> /dev/null &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started successfully."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill "$PID";
            echo "$SERVICE_NAME stopped successfully."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill "$PID";
            echo "$SERVICE_NAME stopped successfully.";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME re-starting ..."
            nohup java -jar "$PATH_TO_JAR" --spring.config.location=file:///$APPLICATION_PROPERTIES_FILE  /tmp 2>> /dev/null >> /dev/null &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME re-started successfully."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
