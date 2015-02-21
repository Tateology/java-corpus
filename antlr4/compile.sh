#!/bin/bash
# Compile runtime
javac -d ./classes -classpath ./lib/org.abego.treelayout.core-1.0.1-sources.jar @COMPILE_ARGS_1
# (Independently) compile tool
javac -d ./classes -classpath ./classes:./lib/ST-4.0.8.jar:./lib/antlr-4.5-complete.jar:./lib/junit-4.12.jar:./lib/hamcrest-core-1.3.jar @COMPILE_ARGS_2
