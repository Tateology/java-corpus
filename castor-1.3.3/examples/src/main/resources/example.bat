@echo off
REM $Id: example.bat 6963 2007-04-23 22:08:53Z rjoachim $
set JAVA=%JAVA_HOME%\bin\java
set cp=%CLASSPATH%
for %%i in (lib\*.jar) do call cp.bat %%i
set cp=%cp%;.

%JAVA% -classpath %CP% %1.Test %2 %3 %4 %5 %6


