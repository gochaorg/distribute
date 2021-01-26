package xyz.cofe.dist.layout

import org.junit.jupiter.api.Test
import xyz.cofe.io.fs.File

import java.nio.file.Paths

class BatchTest {
    @Test
    void test01(){
        Batch batch = new Batch()
        batch.shellScript = Paths.get('bin/batch.bat')
        batch.mainClass = 'xyz.cofe.dist.mvn.tst.console.ConsoleApp'
        batch.jars = Paths.get('lib/jar')

        println( batch.generate() )
        println "-"*40

//        def p = Paths.get('bin/batch.bat')
//        println p
//        println p.nameCount
//        println p.getName(0)
//        println p.getName(1)
//
//        def p2 = Paths.get('batch.bat')
//        println p2.parent
//
//        def p3 = Paths.get('')
//        println p3.nameCount
    }
}
