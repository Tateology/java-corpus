#!/bin/sh
#
# Ant-wrapper

# set this to the directory where your JVM is installed
export JAVA_HOME=/usr/java/jdk1.5.0

# setting environment for freecs
export PATH=$PATH:$JAVA_HOME/bin
export CLASSPATH=$CLASSPATH:ant.jar
export BUILDFILE=./build.xml

$JAVA_HOME/bin/java -cp $CLASSPATH org.apache.tools.ant.Main -buildfile $BUILDFILE $1
