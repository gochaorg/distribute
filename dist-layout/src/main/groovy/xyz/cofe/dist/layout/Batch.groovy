package xyz.cofe.dist.layout

import xyz.cofe.text.Text

import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Batch extends ShellScript {
    Path dll32 = Paths.get('lib/dll/win32')
    Path dll64 = Paths.get('lib/dll/win64')

    String comment( String commentText ){
        if( commentText==null )throw new IllegalArgumentException( "commentText==null" );
        Text.indent("@rem ", commentText )
    }

    private String endl = "\n"
    String getTitleBat(){
        StringBuilder sb = new StringBuilder()
        sb << "automatic generated startup script" << endl
        sb << dateTimeFormatter.format(new Date().toInstant().atZone(ZoneId.systemDefault()))
        comment(sb.toString())+endl
    }

    String generate(){
        if( jars==null )throw new IllegalStateException( "jars==null" );
        if( mainClass==null )throw new IllegalStateException( "mainClass==null" );
        if( dll32==null )throw new IllegalStateException( "dll32==null" );
        if( dll64==null )throw new IllegalStateException( "dll64==null" );

        StringBuilder sb = new StringBuilder()
        sb << getTitleBat();
        sb << "@echo off" << endl;
        sb << endl << "setlocal enabledelayedexpansion" << endl << "setlocal enableextensions" << endl;
        sb << endl;
        sb << "set ERROR_CODE=0" << endl;
        sb << "set BATCH_START_VERBOSE=0" << endl;
        sb << "set CMD_LINE_ARGS=%*" << endl;
        sb << "set THIS_DIR=%~dp0" << endl;
        sb << "set APPNAME=%~n0" << endl;
        sb << "if \"%BATCH_START_VERBOSE%\"==\"1\" echo APPNAME=!APPNAME!" << endl;
        sb << endl;
        sb << "set DEFINE_SYSPROP_APPNAME=1" << endl;
        sb << "set DEFINE_SYSPROP_BASEDIR=1" << endl;
        sb << endl;
        sb << "set MAINCLASS=${mainClass}" << endl;
        sb << "if \"%BATCH_START_VERBOSE%\"==\"1\" echo MAINCLASS=!MAINCLASS!" << endl << endl;

        sb << defAppHome()
        sb << determBitness()
        sb << defJavaLibPath()
        sb << classPath()
        sb << defJavaHome()
        sb << defSysProps()
        sb << exec()

        sb.toString()
    }

    protected String exec(){
        StringBuilder sb = new StringBuilder()
        sb << endl
        sb << comment("execute") << endl
        sb << "\"!JAVA_EXE!\" -cp \"!CLASSPATH!\" !JAVA_OPTS! !MAINCLASS! %CMD_LINE_ARGS%" << endl
        sb << "if %ERRORLEVEL% NEQ 0 goto error" << endl
        sb << "goto end" << endl
        sb << "" << endl
        sb << ":error" << endl
        sb << "if \"%OS%\"==\"Windows_NT\" @endlocal" << endl
        sb << "set ERROR_CODE=%ERRORLEVEL%" << endl
        sb << "" << endl
        sb << ":end" << endl
        sb << ":postExec" << endl
        sb << "if \"%FORCE_EXIT_ON_ERROR%\" == \"on\" (" << endl
        sb << "  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%" << endl
        sb << ")" << endl
        sb << "" << endl
        sb << "exit /B %ERROR_CODE%" << endl
        sb.toString()
    }

    protected String defSysProps(){
        StringBuilder sb = new StringBuilder()
        sb << endl
        sb << comment('set -Dapp.name -Dbasedir') << endl
        sb << "if \"%DEFINE_SYSPROP_APPNAME%\"==\"1\" (" << endl
        sb << "  if defined JAVA_OPTS (" << endl
        sb << "    set \"JAVA_OPTS=!JAVA_OPTS! -Dapp.name=!APPNAME!\"" << endl
        sb << "    if \"%BATCH_START_VERBOSE%\"==\"1\" echo append -Dapp.name=!APPNAME! to JAVA_OPTS" << endl
        sb << "  ) else (" << endl
        sb << "    set \"JAVA_OPTS=-Dapp.name=!APPNAME!\"" << endl
        sb << "    if \"%BATCH_START_VERBOSE%\"==\"1\" echo set -Dapp.name=!APPNAME! to JAVA_OPTS" << endl
        sb << "  )" << endl
        sb << ")" << endl
        sb << "if \"%DEFINE_SYSPROP_BASEDIR%\"==\"1\" (" << endl
        sb << "  if defined JAVA_OPTS (" << endl
        sb << "    set \"JAVA_OPTS=!JAVA_OPTS! -Dbasedir=!APP_HOME!\"" << endl
        sb << "    if \"%BATCH_START_VERBOSE%\"==\"1\" echo append -Dbasedir=!APP_HOME! to JAVA_OPTS" << endl
        sb << "  ) else (" << endl
        sb << "    set \"JAVA_OPTS=-Dbasedir=!APP_HOME!\"" << endl
        sb << "    if \"%BATCH_START_VERBOSE%\"==\"1\" echo set -Dbasedir=!APP_HOME! to JAVA_OPTS" << endl
        sb << "  )" << endl
        sb << ")" << endl
        sb.toString()
    }

    protected String defJavaHome(){
        StringBuilder sb = new StringBuilder()
        sb << endl
        sb << comment('determinate JAVA_HOME, JAVA_EXE, JAVAW_EXE') << endl
        sb << "if not defined JAVA_HOME (" << endl
        sb << "  pushd ." << endl
        sb << "  if exist \"!APP_HOME!\\java\\bin\\java.exe\" (" << endl
        sb << "    cd /d \"!APP_HOME!\\java\"" << endl
        sb << "    set \"JAVA_HOME=!CD!\"" << endl
        sb << "  )" << endl
        sb << "  if exist \"!APP_HOME!\\..\\java\\bin\\java.exe\" (" << endl
        sb << "    cd /d \"!APP_HOME!\\..\\java\"" << endl
        sb << "    set \"JAVA_HOME=!CD!\"" << endl
        sb << "  )" << endl
        sb << "  if %OS_BITNESS% == 32 (" << endl
        sb << "    if exist \"!APP_HOME!\\java\\win32\\bin\\java.exe\" (" << endl
        sb << "      cd /d \"!APP_HOME!\\java\\win32\"" << endl
        sb << "      set \"JAVA_HOME=!CD!\"" << endl
        sb << "    )" << endl
        sb << "    if exist \"!APP_HOME!\\..\\java\\win32\\bin\\java.exe\" (" << endl
        sb << "      cd /d \"!APP_HOME!\\..\\java\\win32\"" << endl
        sb << "      set \"JAVA_HOME=!CD!\"" << endl
        sb << "    )" << endl
        sb << "  )" << endl
        sb << "  if %OS_BITNESS% == 64 (" << endl
        sb << "    if exist \"!APP_HOME!\\java\\win64\\bin\\java.exe\" (" << endl
        sb << "      cd /d \"!APP_HOME!\\java\\win64\"" << endl
        sb << "      set \"JAVA_HOME=!CD!\"" << endl
        sb << "    )" << endl
        sb << "    if exist \"!APP_HOME!\\..\\java\\win64\\bin\\java.exe\" (" << endl
        sb << "      cd /d \"!APP_HOME!\\..\\java\\win64\"" << endl
        sb << "      set \"JAVA_HOME=!CD!\"" << endl
        sb << "    )" << endl
        sb << "  )" << endl
        sb << "  popd" << endl
        sb << ")" << endl
        sb << "if not defined JAVA_HOME (" << endl
        sb << "  java.exe -version >NUL 2>&1" << endl
        sb << "  if errorlevel 1 (" << endl
        sb << "    echo install JAVA or set JAVA_HOME" << endl
        sb << "    exit /b 1" << endl
        sb << "  )" << endl
        sb << "  set JAVA_EXE=java.exe" << endl
        sb << "  set JAVAW_EXE=javaw.exe" << endl
        sb << ") else (" << endl
        sb << "  set \"JAVA_EXE=%JAVA_HOME%\\bin\\java.exe\"" << endl
        sb << "  set \"JAVAW_EXE=%JAVA_HOME%\\bin\\javaw.exe\"" << endl
        sb << "  if \"%BATCH_START_VERBOSE%\"==\"1\" echo JAVA_HOME=!JAVA_HOME!" << endl
        sb << ")" << endl
        sb << "if \"%BATCH_START_VERBOSE%\"==\"1\" echo JAVA_EXE=!JAVA_EXE!" << endl
        sb << "if \"%BATCH_START_VERBOSE%\"==\"1\" echo JAVAW_EXE=!JAVAW_EXE!" << endl
        sb.toString()
    }

    protected String classPath(){
        if( jars==null )throw new IllegalStateException( "jars==null" );

        StringBuilder sb = new StringBuilder()

        def p1 = []
        for( int i=0; i<jars.nameCount; i++ )p1.add( jars.getName(i).toString() )

        sb << endl
        sb << comment('add *.jar to classpath') << endl
        sb << "set \"JARS_DIR=!APP_HOME!\\${p1.join('\\')}\"" << endl
        sb << "if \"%BATCH_START_VERBOSE%\"==\"1\" echo JARS_DIR=!JARS_DIR!" << endl
        sb << "if defined CLASSPATH (" << endl
        sb << "  set \"CLASSPATH=!JARS_DIR!\\*;!CLASSPATH!\"" << endl
        sb << "  if \"%BATCH_START_VERBOSE%\"==\"1\" echo append JARS_DIR to CLASSPATH" << endl
        sb << ")" << endl
        sb << "if not defined CLASSPATH (" << endl
        sb << "  set \"CLASSPATH=!JARS_DIR!\\*\"" << endl
        sb << "  if \"%BATCH_START_VERBOSE%\"==\"1\" echo set JARS_DIR to CLASSPATH" << endl
        sb << ")" << endl

        sb.toString()
    }

    protected String defJavaLibPath(){
        StringBuilder sb = new StringBuilder()

        def p1 = []
        for( int i=0; i<dll32.nameCount; i++ )p1.add( dll32.getName(i).toString() )

        def p2 = []
        for( int i=0; i<dll64.nameCount; i++ )p2.add( dll64.getName(i).toString() )

        sb << endl
        sb << comment('define java.library.path') << endl
        sb << "set \"PREDEF_WIN32_JAVA_LIBPATH=!APP_HOME!\\${p1.join('\\')}\"" << endl
        sb << "set \"PREDEF_WIN64_JAVA_LIBPATH=!APP_HOME!\\${p2.join('\\')}\"" << endl
        sb << "if not defined JAVA_LIBPATH (" << endl
        sb << "  if %OS_BITNESS% == 64 (" << endl
        sb << "    if exist %PREDEF_WIN64_JAVA_LIBPATH% (" << endl
        sb << "      set \"JAVA_LIBPATH=%PREDEF_WIN64_JAVA_LIBPATH%\"" << endl
        sb << "    )" << endl
        sb << "  )" << endl
        sb << "  if %OS_BITNESS% == 32 (" << endl
        sb << "    if exist %PREDEF_WIN32_JAVA_LIBPATH% (" << endl
        sb << "      set \"JAVA_LIBPATH=%PREDEF_WIN32_JAVA_LIBPATH%\"" << endl
        sb << "    )" << endl
        sb << "  )" << endl
        sb << ")" << endl
        sb << "if \"%BATCH_START_VERBOSE%\"==\"1\" echo JAVA_LIBPATH=!JAVA_LIBPATH!" << endl
        sb << "if defined JAVA_LIBPATH (" << endl
        sb << "  if defined JAVA_OPTS (" << endl
        sb << "    set \"JAVA_OPTS=!JAVA_OPTS! -Djava.library.path=!JAVA_LIBPATH!\"" << endl
        sb << "  ) else (" << endl
        sb << "    set \"JAVA_OPTS=-Djava.library.path=!JAVA_LIBPATH!\"" << endl
        sb << "  )" << endl
        sb << ")" << endl

        sb.toString()
    }

    protected String determBitness(){
        StringBuilder sb = new StringBuilder()
        sb << endl
        sb << comment("determinate OS bitness") << endl
        sb << "set OS_BITNESS=64" << endl
        sb << "if %PROCESSOR_ARCHITECTURE% == x86 (" << endl
        sb << "  if not defined PROCESSOR_ARCHITEW6432 (" << endl
        sb << "    set OS_BITNESS=32" << endl
        sb << "  )" << endl
        sb << ")" << endl
        sb << "if \"%BATCH_START_VERBOSE%\"==\"1\" echo OS_BITNESS=!OS_BITNESS!" << endl
        sb.toString()
    }

    protected String defAppHome(){
        StringBuilder sb = new StringBuilder()
        sb << endl
        sb << comment("define home dir of app") << endl
        sb << defPath('APP_HOME',Paths.get(''),shellScript)
        sb.toString()
    }

    protected String defPath( String envVar, Path target, Path batch, boolean appendSlash=false, boolean debug=true ){
        if( envVar==null )throw new IllegalArgumentException( "envVar==null" );
        if( target==null )throw new IllegalArgumentException( "target==null" );
        if( batch==null )throw new IllegalArgumentException( "batch==null" );

        StringBuilder sb = new StringBuilder()
        sb << "if not defined $envVar (" << endl
        sb << "\tpushd ." << endl

        def tpComponents = []
        Path dir = batch.parent
        if( dir!=null ){
            for( int i=0; i<dir.nameCount; i++ ){
                tpComponents.add( '..' )
            }
        }

        for( int i=0; i<target.nameCount; i++ ){
            tpComponents.add( target.getName(i).toString() )
        }
        if( tpComponents.size()==0 ){
            sb << "\tcd /d \"%THIS_DIR%\"" << endl
        }else{
            sb << "\tcd /d \"%THIS_DIR%" << tpComponents.join('\\') << "\"" << endl
        }

        if( appendSlash ){
            sb << "\tset \"${envVar}=!CD!\\\"" << endl
        }else{
            sb << "\tset \"${envVar}=!CD!\"" << endl
        }

        sb << "\tpopd" << endl
        sb << "\tif \"%BATCH_START_VERBOSE%\"==\"1\" echo ${envVar}=!${envVar}!" << endl
        sb << ")" << endl
        sb.toString()
    }
}
