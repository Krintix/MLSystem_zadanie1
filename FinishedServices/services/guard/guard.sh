#!/bin/bash

WORKDIR=$( dirname -- "$0"; )
cd $WORKDIR

source ../java_home_check.sh
nohup "${JAVA_HOME}/bin/java" -jar guard.jar &

exit 0