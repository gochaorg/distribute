package xyz.cofe.dist.layout

import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths

abstract class ShellScriptOp implements Runnable {
    DirOp dirOp
    String filename
    abstract ShellScript getScript()

    //region charset
    Charset charset
    void charset( Charset cs ){
        if( cs==null )throw new IllegalArgumentException( "cs==null" );
        this.charset = cs
    }
    void charset( String cs ){
        if( cs==null )throw new IllegalArgumentException( "cs==null" );
        this.charset = Charset.forName(cs)
    }
    //endregion

    //region mainClass(mainClass)
    void mainClass( String mainClass ){
        if( mainClass==null )throw new IllegalArgumentException( "mainClass==null" );
        getScript().mainClass = mainClass
    }
    //endregion
    //region jars(path)
    void jars( String path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        getScript().jars = Paths.get(path)
    }
    void jars( Path path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        if( !path.isAbsolute() )throw new IllegalArgumentException( "!path.isAbsolute()" );
        getScript().jars = path
    }
    void jars( File path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        if( !path.isAbsolute() )throw new IllegalArgumentException( "!path.isAbsolute()" );
        getScript().jars = toPath(path)
    }
    //endregion

    //region toPath(file):Path
    protected static Path toPath(File file){
        Paths.get( (0 ..< file.nameCount).collect { ni -> file.getName(ni).toString() }.join('/') )
    }
    //endregion
    //region dir() : Eterable<File>
    protected Eterable<File> dirs(){
        return Eterable<DirOp>.tree( dirOp.rootDirOp, from -> from.children
        ).go().map( ts -> ts.getNode().base )
    }
    //endregion

    //region findJarRelativePath()
    protected List<File> findJarDirs(){
        return dirs().findAll {d -> d.name ==~ /(?is)jars?/ }
    }

    @SuppressWarnings('GroovyVariableNotAssigned')
    protected Optional<Path> findJarRelativePath(){
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
    //endregion
}
