#!/bin/bash

JAVA_HOME=~/opt/jre1.8
JAVA_OPTS="-Xmx1024M -Xms1024M -Xss1M"
CLASSPATH=./*:./lib/*

$JAVA_HOME/bin/java $JAVA_OPTS -cp "$CLASSPATH" org.mos91.tcpping.TCPPing $@
