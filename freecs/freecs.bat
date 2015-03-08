@echo off
java -version

set JARS=lib\freecs.jar
set JARS=%JARS%;lib\xmlrpc\commons-codec-1.4.jar
set JARS=%JARS%;lib\xmlrpc\commons-httpclient-3.1.jar

set JARS=%JARS%;lib\xmlrpc\xmlrpc-common-3.1.3.jar
set JARS=%JARS%;lib\xmlrpc\xmlrpc-client-3.1.3.jar
set JARS=%JARS%;lib\xmlrpc\xmlrpc-server-3.1.3.jar

set JARS=%JARS%;lib\xmlrpc\commons-logging-1.1.jar
set JARS=%JARS%;lib\xmlrpc\ws-commons-util-1.0.2.jar

rem # for db-support you must add the jdbc-driver-jar here
rem # for mysql eg:
rem # export JARS=%JARS%;lib\ext\mysql.jar

java -classpath %JARS% freecs.Server

