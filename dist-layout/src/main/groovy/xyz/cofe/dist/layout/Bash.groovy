package xyz.cofe.dist.layout

import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

import java.nio.file.Path

class Bash extends ShellScript {
    String generate(){
        if( mainClass==null )throw new IllegalStateException("mainClass not defined")

        if( jars==null )throw new IllegalStateException("jars not defined")
        if( jars.isAbsolute() )throw new IllegalStateException("jars.isAbsolute()")

        if( shellScript==null )throw new IllegalStateException("shellScript not defined")
        if( shellScript.isAbsolute() )throw new IllegalStateException("shellScript.isAbsolute()")

        StringBuilder sb = new StringBuilder()
        sb << "#!/bin/bash\n"
        sb << "BASH_START_VERBOSE=0\n"
        sb << '''\
            |# resolve links - $0 may be a softlink
            |PRG="$0"
            |
            |while [ -h "$PRG" ]; do
            |  ls=`ls -ld "$PRG"`
            |  link=`expr "$ls" : '.*-> \\(.*\\)$'`
            |  if expr "$link" : '/.*' > /dev/null; then
            |    PRG="$link"
            |  else
            |    PRG=`dirname "$PRG"`/"$link"
            |  fi
            |done
            |
            |'''.stripMargin()

        //Path (0..<bash.nameCount).collect{ni -> }
        Path bashDir = shellScript.parent
        String bashDir2root = bashDir!=null ? (0..<bashDir.nameCount).collect{ni -> '..'}.join('/') : ''
        sb << """\
            |PRGDIR=`dirname "\$PRG"`
            |BASEDIR=`cd "\$PRGDIR/${bashDir2root}" >/dev/null; pwd`
            |[ "\$BASH_START_VERBOSE" == "1" ] && echo BASEDIR=\$BASEDIR
            |
            |CLASSPATH="\$BASEDIR/${jars}/*"
            |[ "\$BASH_START_VERBOSE" == "1" ] && echo CLASSPATH=\$CLASSPATH
            |
            |APP_NAME=\$(basename \$0)
            |[ "\$BASH_START_VERBOSE" == "1" ] && echo APP_NAME=\$APP_NAME
            |
            |MAINCLASS=${mainClass}
            |[ "\$BASH_START_VERBOSE" == "1" ] && echo MAINCLASS=\$MAINCLASS
            |""".stripMargin();

        sb << """\
        |# Reset the REPO variable. If you need to influence this use the environment setup file.
        |REPO=
        |
        |# OS specific support.  \$var _must_ be set to either true or false.
        |cygwin=false;
        |darwin=false;
        |case "`uname`" in
        |  CYGWIN*) cygwin=true ;;
        |  Darwin*) darwin=true
        |           if [ -z "\$JAVA_VERSION" ] ; then
        |             JAVA_VERSION="CurrentJDK"
        |           else
        |             echo "Using Java version: \$JAVA_VERSION"
        |           fi
        |\t\t   if [ -z "\$JAVA_HOME" ]; then
        |\t\t      if [ -x "/usr/libexec/java_home" ]; then
        |\t\t\t      JAVA_HOME=`/usr/libexec/java_home`
        |\t\t\t  else
        |\t\t\t      JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/\${JAVA_VERSION}/Home
        |\t\t\t  fi
        |           fi       
        |           ;;
        |esac
        |
        |if [ -z "\$JAVA_HOME" ] ; then
        |  if [ -r /etc/gentoo-release ] ; then
        |    JAVA_HOME=`java-config --jre-home`
        |  fi
        |fi
        |
        |# For Cygwin, ensure paths are in UNIX format before anything is touched
        |if \$cygwin ; then
        |  [ -n "\$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "\$JAVA_HOME"`
        |  [ -n "\$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "\$CLASSPATH"`
        |fi
        |
        |# If a specific java binary isn't specified search for the standard 'java' binary
        |if [ -z "\$JAVACMD" ] ; then
        |  if [ -n "\$JAVA_HOME"  ] ; then
        |    if [ -x "\$JAVA_HOME/jre/sh/java" ] ; then
        |      # IBM's JDK on AIX uses strange locations for the executables
        |      JAVACMD="\$JAVA_HOME/jre/sh/java"
        |    else
        |      JAVACMD="\$JAVA_HOME/bin/java"
        |    fi
        |  else
        |    JAVACMD=`which java`
        |  fi
        |fi
        |
        |if [ ! -x "\$JAVACMD" ] ; then
        |  echo "Error: JAVA_HOME is not defined correctly." 1>&2
        |  echo "  We cannot execute \$JAVACMD" 1>&2
        |  exit 1
        |fi
        |
        |if [ -z "\$REPO" ]
        |then
        |  REPO="\$BASEDIR"/lib
        |fi
        |
        |#CLASSPATH="\$BASEDIR"/etc:"\$REPO"/*
        |
        |ENDORSED_DIR=
        |if [ -n "\$ENDORSED_DIR" ] ; then
        |  CLASSPATH=\$BASEDIR/\$ENDORSED_DIR/*:\$CLASSPATH
        |fi
        |
        |if [ -n "\$CLASSPATH_PREFIX" ] ; then
        |  CLASSPATH=\$CLASSPATH_PREFIX:\$CLASSPATH
        |fi
        |
        |# For Cygwin, switch paths to Windows format before running java
        |if \$cygwin; then
        |  [ -n "\$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "\$CLASSPATH"`
        |  [ -n "\$JAVA_HOME" ] && JAVA_HOME=`cygpath --path --windows "\$JAVA_HOME"`
        |  [ -n "\$HOME" ] && HOME=`cygpath --path --windows "\$HOME"`
        |  [ -n "\$BASEDIR" ] && BASEDIR=`cygpath --path --windows "\$BASEDIR"`
        |  [ -n "\$REPO" ] && REPO=`cygpath --path --windows "\$REPO"`
        |fi
        |
        |[ "\$BASH_START_VERBOSE" == "1" ] && echo JAVACMD=\$JAVACMD
        |[ "\$BASH_START_VERBOSE" == "1" ] && echo JAVA_OPTS=\$JAVA_OPTS
        |
        |exec "\$JAVACMD" \$JAVA_OPTS  \\
        |  -classpath "\$CLASSPATH" \\
        |  -Dapp.name="\$APP_NAME" \\
        |  -Dapp.pid="\$\$" \\
        |  -Dapp.repo="\$REPO" \\
        |  -Dapp.home="\$BASEDIR" \\
        |  -Dbasedir="\$BASEDIR" \\
        |  \$MAINCLASS \\
        |  "\$@"
        |""".stripMargin()

        return sb.toString()
    }

    private Eterable<File> dirs(){
        return Eterable<DirOp>.tree( dirOp.rootDirOp, from -> from.children
        ).go().map( ts -> ts.getNode().base )
    }

    private List<File> findJarDirs(){
        return dirs().findAll {d -> d.name ==~ /(?is)jars?/ }
    }

    @SuppressWarnings('GroovyVariableNotAssigned')
    private Optional<Path> findJarRelativePath(){
        def jarDirs = findJarDirs()
        if( jarDirs.isEmpty() ){
            println "jar directory not found"
            return Optional.empty()
        }else if( jarDirs.size()>1 ){
            println "found multiple jar directories"
            return Optional.empty()
        }

        File root = dirOp.rootDirOp.base
        File jarDir = jarDirs[0]
        File jarRelative = root.toAbsolute().normalize().relativize( jarDir.toAbsolute().normalize() )

        return Optional.of(toPath(jarRelative))
    }
}
