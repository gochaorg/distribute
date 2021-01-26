package xyz.cofe.dist.mvn;

import groovy.lang.Script
import org.apache.maven.project.MavenProject
import xyz.cofe.dist.layout.DirOp
import xyz.cofe.io.fs.File;

public abstract class LayoutBaseScript extends Script {
    abstract def runScript()
    def preRun() {
        //println "preRunning"
    }
    def postRun() {
        //println "postRunning"
    }
    def run() {
        preRun()
        try {
            runScript()
        } finally {
            postRun()
        }
    }

    AssemblyDirOp assembly( @DelegatesTo(AssemblyDirOp) Closure fn ){
        assembly([:], fn)
    }

    AssemblyDirOp assembly( Map opts, @DelegatesTo(AssemblyDirOp) Closure fn ){
        if( fn==null )throw new IllegalArgumentException( "fn==null" );
        if( project instanceof MavenProject ){
            MavenProject proj = (MavenProject)project

            File asmDir = new File(proj.basedir.toString()).
                resolve("target").
                resolve("pckg").
                resolve("assembly")

            if( !asmDir.exists ){
                asmDir.createDirectories()
            }

            AssemblyDirOp dop = new AssemblyDirOp( asmDir, proj )
            fn.delegate = dop
            fn.resolveStrategy = Closure.DELEGATE_FIRST
            fn(dop)

            dop.postRunJobs.each {it.run() }
            dop
        }
    }
}
