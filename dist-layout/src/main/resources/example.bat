@rem starting cxconsole
@rem by nt.gocha@gmail.com
@echo off

setlocal enabledelayedexpansion
setlocal enableextensions

set ERROR_CODE=0
set BATCH_START_VERBOSE=0
set CMD_LINE_ARGS=%*

rem --------
set THIS_DIR=%~dp0

set APPNAME=%~n0
if "%BATCH_START_VERBOSE%"=="1" echo APPNAME=!APPNAME!

set DEFINE_SYSPROP_APPNAME=1
set DEFINE_SYSPROP_BASEDIR=1

set MAINCLASS=xyz.cofe.dist.mvn.tst.console.ConsoleApp
if "%BATCH_START_VERBOSE%"=="1" echo MAINCLASS=!MAINCLASS!

rem define home dir of app
if not defined APP_HOME (
	pushd .

	rem echo cd to %THIS_DIR%..
	cd /d %THIS_DIR%..

	rem echo cd !CD!
	set "APP_HOME=!CD!\"

	popd
	if "%BATCH_START_VERBOSE%"=="1" echo APP_HOME=!APP_HOME!
)

rem determinate OS bitness
set OS_BITNESS=64

if %PROCESSOR_ARCHITECTURE% == x86 (
  if not defined PROCESSOR_ARCHITEW6432 (
    :: echo OS32
	set OS_BITNESS=32
  )
)

if "%BATCH_START_VERBOSE%"=="1" echo OS_BITNESS=!OS_BITNESS!

rem DLL ??????????
set "PREDEF_WIN32_JAVA_LIBPATH=!APP_HOME!lib\dll\win32"
set "PREDEF_WIN64_JAVA_LIBPATH=!APP_HOME!lib\dll\win64"

if not defined JAVA_LIBPATH (
	rem echo check lib path %OS_BITNESS%
	if %OS_BITNESS% == 64 (
		rem echo os 64
		if exist %PREDEF_WIN64_JAVA_LIBPATH% (
			set "JAVA_LIBPATH=%PREDEF_WIN64_JAVA_LIBPATH%"
			rem echo lib path = !JAVA_LIBPATH!
		)
	)

	if %OS_BITNESS% == 32 (
		rem echo os 32
		if exist %PREDEF_WIN32_JAVA_LIBPATH% (
			set "JAVA_LIBPATH=%PREDEF_WIN32_JAVA_LIBPATH%"
			rem echo lib path = !JAVA_LIBPATH!
		)
	)
)

if "%BATCH_START_VERBOSE%"=="1" echo JAVA_LIBPATH=!JAVA_LIBPATH!

if defined JAVA_LIBPATH (
	rem set JAVA_OPTS=!JAVA_OPTS! -Djava.library.path=!JAVA_LIBPATH!
	if defined JAVA_OPTS (
		set "JAVA_OPTS=!JAVA_OPTS! -Djava.library.path=!JAVA_LIBPATH!"
		if "%BATCH_START_VERBOSE%"=="1" echo append JAVA_LIBPATH to JAVA_OPTS
	) else (
		set "JAVA_OPTS=-Djava.library.path=!JAVA_LIBPATH!"
		if "%BATCH_START_VERBOSE%"=="1" echo set JAVA_LIBPATH to JAVA_OPTS
	)

	set "PATH=!JAVA_LIBPATH!;%PATH%"
	if "%BATCH_START_VERBOSE%"=="1" echo append JAVA_LIBPATH to PATH
)

rem ??????? ? jar ???????
set "JARS_DIR=!APP_HOME!lib\jar"
if "%BATCH_START_VERBOSE%"=="1" echo JARS_DIR=!JARS_DIR!

rem build classpath
if defined CLASSPATH (
	set "CLASSPATH=!JARS_DIR!\*;!CLASSPATH!"
	if "%BATCH_START_VERBOSE%"=="1" echo append JARS_DIR to CLASSPATH
)

if not defined CLASSPATH (
	set "CLASSPATH=!JARS_DIR!\*"
	if "%BATCH_START_VERBOSE%"=="1" echo set JARS_DIR to CLASSPATH
)

rem ???????????? JAVA ? ????????????
if not defined JAVA_HOME (
	pushd .
	rem echo cd to %THIS_DIR%..
	cd /d %THIS_DIR%..
	if %OS_BITNESS% == 32 (
		if exist !APP_HOME!java\bin\java.exe (
			cd !APP_HOME!java
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!..\java\bin\java.exe (
			cd !APP_HOME!..\java
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!..\..\java\bin\java.exe (
			cd !APP_HOME!..\..\java
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!java\win32\bin\java.exe (
			cd !APP_HOME!java\win32
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!..\java\win32\bin\java.exe (
			cd !APP_HOME!..\java\win32
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!..\..\java\win32\bin\java.exe (
			cd !APP_HOME!..\..\java\win32
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)
	) else (
		if exist !APP_HOME!java\bin\java.exe (
			cd !APP_HOME!java
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!..\java\bin\java.exe (
			cd !APP_HOME!..\java
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!..\..\java\bin\java.exe (
			cd !APP_HOME!..\..\java
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!java\win64\bin\java.exe (
			cd !APP_HOME!java\win64
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!..\java\win64\bin\java.exe (
			cd !APP_HOME!..\java\win64
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)

		if exist !APP_HOME!..\..\java\win64\bin\java.exe (
			cd !APP_HOME!..\..\java\win64
			set JAVA_HOME=!CD!
			if "%BATCH_START_VERBOSE%"=="1" echo found bundled java JAVA_HOME=!JAVA_HOME!
		)
	)

	popd
)

if not defined JAVA_HOME (
	java.exe -version >NUL 2>&1
	if errorlevel 1 (
		echo install JAVA or set JAVA_HOME
		exit /b 1
	)

	set JAVA_EXE=java.exe
	set JAVAW_EXE=javaw.exe
	if "%BATCH_START_VERBOSE%"=="1" echo JAVA_EXE=!JAVA_EXE!
	if "%BATCH_START_VERBOSE%"=="1" echo JAVAW_EXE=!JAVAW_EXE!
) else (
	set JAVA_EXE=%JAVA_HOME%\bin\java.exe
	set JAVAW_EXE=%JAVA_HOME%\bin\javaw.exe
	if "%BATCH_START_VERBOSE%"=="1" echo JAVA_HOME=!JAVA_HOME!
	if "%BATCH_START_VERBOSE%"=="1" echo JAVA_EXE=!JAVA_EXE!
	if "%BATCH_START_VERBOSE%"=="1" echo JAVAW_EXE=!JAVAW_EXE!
)

if "%DEFINE_SYSPROP_APPNAME%"=="1" (
	if defined JAVA_OPTS (
		set "JAVA_OPTS=!JAVA_OPTS! -Dapp.name=!APPNAME!"
		if "%BATCH_START_VERBOSE%"=="1" echo append -Dapp.name=!APPNAME! to JAVA_OPTS
	) else (
		set "JAVA_OPTS=-Dapp.name=!APPNAME!"
		if "%BATCH_START_VERBOSE%"=="1" echo set -Dapp.name=!APPNAME! to JAVA_OPTS
	)
)

if "%DEFINE_SYSPROP_BASEDIR%"=="1" (
	if defined JAVA_OPTS (
		set "JAVA_OPTS=!JAVA_OPTS! -Dbasedir=!APP_HOME!"
		if "%BATCH_START_VERBOSE%"=="1" echo append -Dbasedir=!APP_HOME! to JAVA_OPTS
	) else (
		set "JAVA_OPTS=-Dbasedir=!APP_HOME!"
		if "%BATCH_START_VERBOSE%"=="1" echo set -Dbasedir=!APP_HOME! to JAVA_OPTS
	)
)

::%JAVACMD% %JAVA_OPTS%  -classpath %CLASSPATH% -Dapp.name="cxconsole" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" xyz.cofe.cxconsole.CxConsole %CMD_LINE_ARGS%
::if %ERRORLEVEL% NEQ 0 goto error

"!JAVA_EXE!" -cp "!CLASSPATH!" !JAVA_OPTS! !MAINCLASS! %CMD_LINE_ARGS%
if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=%ERRORLEVEL%

:end
::@REM If error code is set to 1 then the endlocal was done already in :error.
::if %ERROR_CODE% EQU 0 @endlocal

:postExec
if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
