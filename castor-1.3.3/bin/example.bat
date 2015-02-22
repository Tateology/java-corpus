@echo off
REM $Id: example.bat 5951 2006-05-30 22:18:48Z bsnyder $
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=..\build\classes;..\build\examples;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (..\lib\*.jar) do call cp.bat %%i

%JAVA% -classpath %CP% %1.Test %2 %3 %4 %5 %6


