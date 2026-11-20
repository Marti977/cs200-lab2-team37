@echo off
setlocal
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
set "JAVAC=javac"
set "JAVA=java"
if defined JAVA_HOME set "JAVAC=%JAVA_HOME%\bin\javac.exe"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java.exe"
"%JAVAC%" --release 17 -Xlint:all -d build\classes src\chocan\*.java
if errorlevel 1 exit /b 1
"%JAVA%" -cp build\classes chocan.Main
