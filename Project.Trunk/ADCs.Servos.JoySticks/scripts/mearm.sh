#!/bin/bash
#
CP=../build/libs/ADCs.Servos.Joysticks-1.0-all.jar
#
JAVA_OPTIONS=
# JAVA_OPTIONS="$JAVA_OPTIONS -Djoystick.verbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dmearm.pilot=false"
# For remote debugging:
# JAVA_OPTIONS="$JAVA_OPTIONS -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
# For remote JVM Monitoring
# JAVA_OPTIONS="$JAVA_OPTIONS -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=raspberrypi-boat"
echo Running...
sudo java $JAVA_OPTIONS -cp $CP paddle.ForMeArm
