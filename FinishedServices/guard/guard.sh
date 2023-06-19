#!/bin/bash

JAVA_HOME=usr/lib/jvm/jdk-17
WORKDIR=$( dirname -- "$0"; )

cd $WORKDIR
nohup "/${JAVA_HOME}/bin/java" -jar guard.jar &

exit 0