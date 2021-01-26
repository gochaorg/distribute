package xyz.cofe.dist.layout

import xyz.cofe.dist.layout.Batch
import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths

class BatchOp extends ShellScriptOp {
    Charset charset = Charset.forName('windows-1251')

    private final Batch script = new Batch()
    @Override ShellScript getScript() { return this.@script }

    //region dll32
    void dll32( String path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        batch.dll32 = Paths.get(path)
    }
    void dll32( Path path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        if( !path.isAbsolute() )throw new IllegalArgumentException( "!path.isAbsolute()" );
        batch.dll32 = path
    }
    void dll32( File path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        if( !path.isAbsolute() )throw new IllegalArgumentException( "!path.isAbsolute()" );
        batch.dll32 = toPath(path)
    }
    //endregion
    //region dll64
    void dll64( String path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        batch.dll64 = Paths.get(path)
    }
    void dll64( Path path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        if( !path.isAbsolute() )throw new IllegalArgumentException( "!path.isAbsolute()" );
        batch.dll64 = path
    }
    void dll64( File path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        if( !path.isAbsolute() )throw new IllegalArgumentException( "!path.isAbsolute()" );
        batch.dll64 = toPath(path)
    }
    //endregion

    @Override
    void run() {
        if( dirOp==null )throw new IllegalStateException("dirOp == null")
        if( filename==null )throw new IllegalStateException("filename == null")

        File root = dirOp.rootDirOp.base
        File batchFile = dirOp.base.resolve(filename)

        if( script.shellScript==null ) {
            File batchRelative = root.toAbsolute().normalize().relativize( batchFile.toAbsolute().normalize() )
            script.shellScript = toPath(batchRelative)
        }

        if( script.jars==null ){
            def jarPath = findJarRelativePath()
            if( jarPath.isPresent() ){
                script.jars = jarPath.get()
            }else{
                throw new IllegalStateException("can't init batch.jars")
            }
        }

        batchFile.writeText( script.generate(), charset )
        println "generated ${root.toAbsolute().normalize().relativize(batchFile.toAbsolute().normalize())}"
    }
}
