#!/usr/bin/env bash
#
CP=build/libs/http-tiny-server-1.0-all.jar
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose.dump=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.client.verbose=true"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.util.logging.config.file=logging.properties"
#
java -cp ${CP} ${JAVA_OPTIONS} http.HTTPServer
#
echo -e "Try doing a curl http://localhost:10000/"
