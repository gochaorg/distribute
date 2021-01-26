package xyz.cofe.dist.layout

import xyz.cofe.io.fs.File

import java.nio.file.Path
import java.nio.file.Paths

class ProgramOp implements Runnable {
    private DirOp dirOp
    public DirOp getDirOp(){ return this.@dirOp }
    public void setDirOp( DirOp v ){
        if( v==null )throw new IllegalArgumentException( "v==null" );
        this.@dirOp = v
        shellScriptOps.each {it.dirOp = v }
    }

    private String basename
    public String getBasename(){
        return this.@basename
    }
    public void setBasename( String name ){
        if( name==null )throw new IllegalArgumentException( "name==null" );
        this.basename = name;

        bashOp.filename = name+".sh"
        batchOp.filename = name+".bat"
    }

    void mainClass( String mainClassname ){
        if( mainClassname==null )throw new IllegalArgumentException( "mainClassname==null" );
        shellScriptOps.each { it.mainClass(mainClassname) }
    }

    void jars( String path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        shellScriptOps.each { it.jars(path) }
    }
    void jars(Path path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        if( !path.isAbsolute() )throw new IllegalArgumentException( "!path.isAbsolute()" );
        shellScriptOps.each { it.jars(path) }
    }
    void jars(File path ){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        if( !path.isAbsolute() )throw new IllegalArgumentException( "!path.isAbsolute()" );
        shellScriptOps.each { it.jars(path) }
    }

    boolean generateBash = true
    final BashOp bashOp = new BashOp()

    public class BashConf {
        @Delegate BashOp bashOp;
        boolean isEnable(){ return generateBash }
        void setEnable( boolean v ){
            generateBash = v
        }
        BashConf enable( boolean v ){
            generateBash = v
            this
        }
    }
    final BashConf bash = new BashConf(bashOp: this.bashOp)

    boolean generateBatch = true
    final BatchOp batchOp = new BatchOp()

    public class BatchConf {
        @Delegate BatchOp batchOp
        boolean isEnable(){ return generateBatch }
        void setEnable( boolean v ){
            generateBatch = v
        }
        BatchConf enable( boolean v ){
            generateBatch = v
            this
        }
    }
    final BatchConf batch = new BatchConf(batchOp: batchOp)

    protected final List<ShellScriptOp> shellScriptOps = [ bashOp, batchOp ]

    @Override
    void run() {
        if( generateBash ){
            bashOp.run()
        }
        if( generateBatch ){
            batchOp.run()
        }
    }
}
