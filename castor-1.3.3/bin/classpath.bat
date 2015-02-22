@echo off
REM $Id: classpath.bat 5951 2006-05-30 22:18:48Z bsnyder $
set JAVA=%JAVA_HOME%\bin\java
set OLDCP=%CLASSPATH%
set CLASSPATH=..\build\classes;..\build\tests;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (..\lib\*.jar) do call cp.bat %%i

%1 -classpath %CP% %2 %3 %4 %5 %6 %7 %8 %9
set CLASSPATH=%OLDCP%
set OLDCP=