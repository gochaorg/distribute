package xyz.cofe.dist.mvn

import org.apache.maven.artifact.Artifact
import org.apache.maven.project.MavenProject

class ExecContext {
    private static final InheritableThreadLocal<ExecContext> context =
        new InheritableThreadLocal<ExecContext>() {
            @Override
            protected ExecContext initialValue() {
                return new ExecContext();
            }
        };

    public static ExecContext get(){
        context.get()
    }

    MavenProject project

    public static boolean isProject( Artifact a ){
        if( a==null )throw new IllegalArgumentException( "a==null" );

        MavenProject proj = context.get().project
        if( proj==null )return false

        if( a == proj.artifact )return true

        if( a.groupId==proj.groupId &&
            a.artifactId==proj.artifact.artifactId &&
            a.version==proj.artifact.version &&
            a.classifier==proj.artifact.classifier &&
            a.type == proj.artifact.type
        ){
            return true
        }

        return false
    }
}
