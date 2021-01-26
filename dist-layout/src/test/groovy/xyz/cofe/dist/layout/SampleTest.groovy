package xyz.cofe.dist.layout

import org.junit.jupiter.api.Test
import xyz.cofe.io.fs.File

class SampleTest {
    public void exec( DirOp dirOp, @DelegatesTo(DirOp) Closure script ){
        script.resolveStrategy = Closure.DELEGATE_FIRST
        script.delegate = dirOp
        script(dirOp)
    }

    @Test
    public void test01(){
        DirOp dirOp = new DirOp(new File('target/SampleTest/test01'))
        exec(dirOp) {
            clear()
            dir("bin"){
                text("hello.txt" ) { PrintWriter out ->
                    out.println "hello"
                }
            }
            dir("lib") {
                dir("jars")
            }
            copy(new File("/home/user/code/distribute/dist-layout/pom.xml"))

            chmod(tree.filesOnly(), "o-rwx")

            File from = base
            new PrintFiles().configure( pf -> {
                pf.resolver = (File relf) -> from.resolve(relf)
                pf.files = tree(true)
            }).print( System.out )

            dir("some"){
                copy("/home/user/code/distribute/dist-layout/target/classes/xyz/cofe/dist")
            }
        }
    }

    @Test
    public void test02(){
        DirOp dirOp = new DirOp(new File('target/SampleTest/test02'))
        exec(dirOp) {
            clear()
            dir("bin"){
                String mainClassName = 'xyz.cofe.dist.mvn.tst.console.ConsoleApp'
                batch("batch.bat") {
                    mainClass(mainClassName)
                }
                bash( 'console.sh' ) {
                    mainClass(mainClassName)
                }
            }
            dir("lib"){
                dir("jar")
            }
        }
        dirOp.postRunJobs.each {it.run() }
    }

    @Test
    public void test03(){
        DirOp dirOp = new DirOp(new File('target/SampleTest/test03'))
        exec(dirOp) {
            clear()
            dir("bin"){
//                String mainClassName = 'xyz.cofe.dist.mvn.tst.console.ConsoleApp'
//                batch("batch.bat") {
//                    mainClass(mainClassName)
//                }
//                bash( 'console.sh' ) {
//                    mainClass(mainClassName)
//                }
                program('console'){
                    mainClass 'xyz.cofe.dist.mvn.tst.console.ConsoleApp'
                }
            }
            dir("lib"){
                dir("jar")
            }
        }
        dirOp.postRunJobs.each {it.run() }
    }
}
