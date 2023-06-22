#!/bin/bash
#If JAVA_HOME is not set as environment variable, provide it in ALT_JAVA_HOME
ALT_JAVA_HOME="/usr/lib/jvm/jdk-17"
if [ "$JAVA_HOME" = "" ]; then 
	echo "WARN JAVA_HOME not set. Using[ $ALT_JAVA_HOME ]"
	export JAVA_HOME="$ALT_JAVA_HOME"
fi