package xyz.cofe.dist.mvn

import org.apache.maven.artifact.Artifact
import org.apache.maven.project.MavenProject
import xyz.cofe.iter.Eterable

import java.util.function.Predicate

class Artifacts implements Eterable<Artifact> {
    @SuppressWarnings('GrFinalVariableAccess')
    private final Eterable<Artifact> artifacts;

    public Artifacts(Eterable<Artifact> artifacts){
        if( artifacts==null )throw new IllegalArgumentException( "artifacts==null" );
        this.artifacts = artifacts
    }

    public Artifacts( MavenProject project ){
        if( project==null )throw new IllegalArgumentException( "project==null" );
        this.artifacts = Eterable.of(project.artifacts)
    }

    public Iterator<Artifact> iterator(){ return artifacts.iterator() }

    /**
     * Фильтрация исходных данных
     * @param filter фильтр
     * @return итератор
     */
    @Override Artifacts filter(Predicate<Artifact> filter) {
        return new Artifacts( artifacts.filter(filter) )
    }

    /**
     * Фильтрация исходного набора - удаление null значений
     * @return итератор
     */
    @Override Artifacts notNull() {
        return new Artifacts( artifacts.notNull() )
    }

    /**
     * Присоединение данных к исходным
     * @param iter присоединяемые данные
     * @return итератор
     */
    @Override Artifacts union(Iterable<Artifact>... iter) {
        return new Artifacts( artifacts.union(iter) )
    }

    /**
     * Присоединение данных к исходным
     * @param iter присоединяемые данные
     * @return итератор
     */
    @Override Artifacts union(Iterable<Iterable<Artifact>> iter) {
        return new Artifacts( artifacts.union(iter) )
    }

    /**
     * Ограничение исходного набора
     * @param limit максимаьлное кол-во выбираемых значений
     * @return кол-во
     */
    @Override Artifacts limit(long limit) {
        return new Artifacts( artifacts.limit(limit) )
    }

    public class ScopeFilter {
        public Artifacts getCompile(){
            return filter( a -> 'compile'.equalsIgnoreCase(a.scope) )
        }
        public Artifacts getProvided(){
            return filter( a -> 'provided'.equalsIgnoreCase(a.scope) )
        }
        public Artifacts getRuntime(){
            return filter( a -> 'runtime'.equalsIgnoreCase(a.scope) )
        }
        public Artifacts getTest(){
            return filter( a -> 'test'.equalsIgnoreCase(a.scope) )
        }
        public Artifacts getSystem(){
            return filter( a -> 'system'.equalsIgnoreCase(a.scope) )
        }
        public Artifacts getImport(){
            return filter( a -> 'import'.equalsIgnoreCase(a.scope) )
        }
        public Artifact getProject(){
            return filter( a -> ExecContext.isProject(a) )
        }

        public Artifacts find(
            boolean compile,
            boolean provided,
            boolean runtime,
            boolean test,
            boolean system,
            boolean impord
        ){
            return filter( a ->
                ( impord && a.scope!=null && 'import'.equalsIgnoreCase(a.scope) ) ||
                ( system && a.scope!=null && 'system'.equalsIgnoreCase(a.scope) ) ||
                ( test && a.scope!=null && 'test'.equalsIgnoreCase(a.scope) ) ||
                ( runtime && a.scope!=null && 'runtime'.equalsIgnoreCase(a.scope) ) ||
                ( provided && a.scope!=null && 'provided'.equalsIgnoreCase(a.scope) ) ||
                ( compile && a.scope!=null && 'compile'.equalsIgnoreCase(a.scope) )
            )
        }
    }

    public final ScopeFilter scope = new ScopeFilter()
}
