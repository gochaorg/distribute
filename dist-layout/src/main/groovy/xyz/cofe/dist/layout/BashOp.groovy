package xyz.cofe.dist.layout

import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths

class BashOp extends ShellScriptOp {
    Charset charset = Charset.forName('utf-8')

    //region script : ShellScript
    private final Bash script = new Bash()
    @Override ShellScript getScript() { return this.@script }
    //endregion

    @Override
    void run() {
        if( dirOp==null )throw new IllegalStateException("dirOp == null")
        if( filename==null )throw new IllegalStateException("filename == null")

        File root = dirOp.rootDirOp.base
        File bashFile = dirOp.base.resolve(filename)

        if( script.mainClass==null ){
            throw new IllegalStateException("mainClass not defined");
        }

        if( script.shellScript==null ) {
            File bashRelative = root.toAbsolute().normalize().relativize( bashFile.toAbsolute().normalize() )
            script.shellScript = toPath(bashRelative)
        }

        if( script.jars==null ){
            def jarPath = findJarRelativePath()
            if( jarPath.isPresent() ){
                script.jars = jarPath.get()
            }else{
                throw new IllegalStateException("can't init bash.jars")
            }
        }

        bashFile.writeText(
            script.generate(),
            charset ?: Charset.defaultCharset()
        )

        println "generated ${root.toAbsolute().normalize().relativize(bashFile.toAbsolute().normalize())}"

        new Chmod().configure({c -> c.files = Eterable.single(bashFile)}
        ).append.owner(true).group(true).others(true).read(true).execute(true).run()
    }
}
