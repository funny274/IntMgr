@echo off
title Switch JDK version

:: =====================================================================
::  switch-jdk.bat  -  Quickly switch between JDK 17 and JDK 25
:: =====================================================================
::  Usage: double-click this file, or run it from cmd in this folder.
::
::  What it does:
::    1) "Current window" (always happens): sets JAVA_HOME to the chosen
::       JDK and puts its \bin at the FRONT of PATH, effective immediately.
::    2) "Permanent" (optional): uses setx to save JAVA_HOME for your
::       current user (no admin rights needed), so new windows also use it.
::
::  If you move a JDK later, just edit the two paths below.
:: =====================================================================

set "JDK17=C:\Users\issuser\.jdks\jdk-17.0.2"
set "JDK25=C:\Program Files\Java\jdk-25.0.3"

:menu
echo.
echo ==========================================
echo   Select a JDK version:
echo     1)  JDK 17   ^(recommended for this project^)
echo     2)  JDK 25
echo     0)  Exit without changes
echo ==========================================
set "choice="
set /p "choice=Enter 1 / 2 / 0 : "

if "%choice%"=="1" (
    set "TARGET=%JDK17%"
    set "LABEL=JDK 17"
) else if "%choice%"=="2" (
    set "TARGET=%JDK25%"
    set "LABEL=JDK 25"
) else if "%choice%"=="0" (
    echo Exited. Nothing changed.
    goto :end
) else (
    echo [Invalid input] Please enter 1, 2 or 0.
    goto :menu
)

if not exist "%TARGET%\bin\java.exe" (
    echo.
    echo [Error] Not found: %TARGET%\bin\java.exe
    echo         Please edit this file and fix the JDK17 / JDK25 paths.
    goto :menu
)

:: 1) Take effect in this window immediately
set "JAVA_HOME=%TARGET%"
set "PATH=%TARGET%\bin;%PATH%"

:: 2) Optionally persist JAVA_HOME for the current user
set "persist="
set /p "persist=Also save JAVA_HOME permanently for your user? (y/n, default n) : "
if /i "%persist%"=="y" (
    setx JAVA_HOME "%TARGET%" >nul
    echo [OK] Saved user env var JAVA_HOME = %TARGET%
) else (
    echo [Info] This only applies to the current window.
)

echo.
echo Switched to %LABEL%
echo JAVA_HOME = %JAVA_HOME%
echo Java version:
java -version 2>&1
echo.

:end
echo Press any key to exit...
pause >nul