package xyz.cofe.dist.mvn;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import xyz.cofe.io.fs.File;

@Mojo(
    name = "pckg",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class LayoutMojo extends AbstractMojo {
    @Parameter(defaultValue = '${project}', required = true, readonly = true)
    private MavenProject project;

    @Parameter(required = true, readonly = true)
    private List<String> scripts;

    @Parameter(required = false, defaultValue = "utf-8")
    private String encoding;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ExecContext.get().project = project
        if( scripts!=null ){
            for( String script : scripts ){
                if( script==null )continue;
                executeScriptFile(script);
            }
        }
    }

    private Charset scriptCharset(){
        if( encoding==null ){
            return StandardCharsets.UTF_8;
        }else{
            return Charset.forName(encoding);
        }
    }

    private void executeScriptFile( String scriptFileName ){
        File file = new File(scriptFileName);
        if( file.isFile() ){
            executeScript(file.readText(scriptCharset()));
        }else{
            getLog().error("Script file "+file+" not found");
        }
    }

    private void executeScript( String scriptSource ){
        CompilerConfiguration cc = new CompilerConfiguration();

        ImportCustomizer impc = new ImportCustomizer();
        cc.addCompilationCustomizers(impc);
        cc.setScriptBaseClass(LayoutBaseScript.class.getName());

        impc.addImports(File.class.getName());

        GroovyShell gs = new GroovyShell(cc);
        gs.setVariable("project", project);

        Script script = null;
        try {
            script = gs.parse(scriptSource);
        } catch( Throwable err ){
            getLog().error("Groovy script parse error",err);
            return;
        }

        try {
            script.run();
        } catch( Throwable err ){
            getLog().error("Groovy script execution error",err);
        }
    }
}
