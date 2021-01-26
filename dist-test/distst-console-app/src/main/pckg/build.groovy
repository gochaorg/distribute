println "build assembly for $project"

//noinspection GroovyAssignabilityCheck
assembly {
    println "assembly directory "+base
    clear()

    dir( "bin" ) {
        program('console'){
            mainClass 'xyz.cofe.dist.mvn.tst.console.ConsoleApp'
        }
    }
    dir( 'lib' ) {
        dir( 'jar' ) {
            copy(projArtifacts)
        }
    }
}
