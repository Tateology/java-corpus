@echo off
REM $Id: test.bat 7694 2008-07-07 22:05:45Z wguttmn $
set JAVA=%JAVA_HOME%\bin\java
set OLDCP=%CLASSPATH%
set CLASSPATH=..\build\tests;..\build\classes;..\cpa\build\classes;%CLASSPATH%
set CLASSPATH=..\core\build\classes;%CLASSPATH%
set CLASSPATH=..\cpactf\build\classes;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (..\lib\*.jar) do call cp.bat %%i
for %%i in (..\lib\tests\*.jar) do call cp.bat %%i

REM add something like the following to include
REM database driver to your classpath
REM
REM for %%i in (..\..\castor\lib\run\*.jar) do call cp.bat %%i

"%JAVA%" -classpath "%CP%" MainApp %1 %2 %3 %4 %5 %6
set CLASSPATH=%OLDCP%