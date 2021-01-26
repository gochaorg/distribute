package xyz.cofe.dist.mvn

import org.apache.maven.artifact.Artifact
import org.apache.maven.project.MavenProject
import xyz.cofe.dist.layout.Batch
import xyz.cofe.dist.layout.DirOp
import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

class AssemblyDirOp extends DirOp {
    AssemblyDirOp( File f, MavenProject project ){
        super( f )
        if( project==null )throw new IllegalArgumentException( "project==null" );
        this.project = project
    }
    AssemblyDirOp( File f, MavenProject project, AssemblyDirOp parent ){
        super( f, parent )
        if( project==null )throw new IllegalArgumentException( "project==null" );
        this.project = project
    }

    protected MavenProject project

    //region cd() / dir() - fix return type
    @Override
    AssemblyDirOp cd(Map opts, File dir) {
        if( opts==null )opts = [:]
        return new AssemblyDirOp(dir, project, this)
    }

    @Override
    def dir(String dirname, @DelegatesTo(AssemblyDirOp) Closure content) {
        return super.dir(dirname, content)
    }

    @Override
    def dir(File dir, @DelegatesTo(AssemblyDirOp) Closure content) {
        return super.dir(dir, content)
    }
    //endregion

    //region copy dependencies
    Artifacts getArtifacts(){ new Artifacts(project) }

    Artifact getArtifact(){ project.artifact }

    void copy( Artifacts artifacts ){
        if( artifacts==null )throw new IllegalArgumentException( "artifacts==null" );
        for( Artifact a : artifacts ){
            if( a.file!=null ){
                copy(new File(a.file.toString()))
            }
        }
    }

    Artifacts getDeps(){
        Iterable<Artifact> i = Eterable.of(project.artifact)
        Iterable[] ia = new Iterable[]{ i }
        return getArtifacts().union( ia )
    }

    Artifacts getProjArtifacts(){
        getDeps().filter(
            a -> a.scope==null || a.scope=='compile' || a.scope=='runtime'
        )
    }
    //endregion
}
