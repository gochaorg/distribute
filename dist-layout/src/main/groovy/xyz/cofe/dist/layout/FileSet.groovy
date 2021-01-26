package xyz.cofe.dist.layout

import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

import java.util.function.Predicate

class FileSet implements Eterable<File> {
    //region create
    protected final Eterable<File> files;

    public FileSet(Eterable<File> files){
        this.files = files;
        if( files==null )throw new IllegalArgumentException( "files==null" );
    }

    public Iterator<File> iterator(){ return files.iterator(); }
    public static FileSet of( File file ){
        if( file==null )throw new IllegalArgumentException( "file==null" );
        return new FileSet( Eterable.of(file) );
    }
    public static FileSet of( Iterable<File> files ){
        if( files==null )throw new IllegalArgumentException( "files==null" );
        return new FileSet( Eterable.of(files).filter(Objects::nonNull) );
    }
    public static FileSet tree( File file ){
        if( file==null )throw new IllegalArgumentException( "file==null" );
        return new FileSet( file.walk().go() )
    }
    //endregion

    //region common eterable methods
    /**
     * Фильтрация исходных данных
     * @param filter фильтр
     * @return итератор
     */
    @Override
    public FileSet filter(Predicate<File> filter){
        return new FileSet(files.filter(filter));
    }

    /**
     * Фильтрация исходного набора - удаление null значений
     *
     * @return итератор
     */
    @Override
    public FileSet notNull(){
        return new FileSet(files.notNull());
    }

    /**
     * Присоединение данных к исходным
     *
     * @param iter присоединяемые данные
     * @return итератор
     */
    @Override
    public FileSet union(Iterable<File>... iter){
        return new FileSet(files.union(iter));
    }

    /**
     * Присоединение данных к исходным
     *
     * @param iter присоединяемые данные
     * @return итератор
     */
    @Override
    public FileSet union(Iterable<Iterable<File>> iter){
        return new FileSet(files.union(iter));
    }

    /**
     * Ограничение исходного набора
     *
     * @param limit максимаьлное кол-во выбираемых значений
     * @return кол-во
     */
    @Override
    public FileSet limit(long limit){
        return new FileSet(files.limit(limit));
    }
    //endregion
    //region toAbsolute() / relativize()
    /**
     * Преобразование к абсолютным путям
     * @param root корень
     * @return абсолютные пути
     */
    public FileSet toAbsolute( File root ){
        if( root==null )throw new IllegalArgumentException( "root==null" );
        return new FileSet( this.map( { f ->
            return f.isAbsolute() ? f : root.resolve(f)
        }))
    }

    /**
     * Преобразование к относительным путям
     * @param root корень
     * @return относительные пути
     */
    public FileSet relativize( File root ){
        if( root==null )throw new IllegalArgumentException( "root==null" );
        return new FileSet( this.map( { f ->
            return root.relativize(f)
        }))
    }
    //endregion

    /**
     * Получение только списка файлов
     * @return список файлов
     */
    public FileSet filesOnly() {
        return filter( f -> f.isFile() )
    }

    /**
     * Получение только списка каталогов
     * @return список каталогов
     */
    public FileSet dirsOnly(){
        return filter( f -> f.isDir() )
    }
}
