package xyz.cofe.dist.layout

import xyz.cofe.io.fn.IOFun
import xyz.cofe.io.fs.File

import java.util.function.Consumer
import java.util.function.Predicate

class CopyJob implements Runnable {
    File from
    CopyJob from(File file ){
        if( file==null )throw new IllegalArgumentException( "file==null" );
        this.from = file
        this
    }

    File to
    CopyJob to(File file ){
        if( file==null )throw new IllegalArgumentException( "file==null" );
        this.to = file
        this
    }

    /**
     * Listeners
     * where listener is fn( err: Throwable )
     */
    final List<Closure> onError = []

    /**
     * listeners.
     * where listener is fn( File from, File to, long size, long timeMilliSec )
     */
    final List<Closure> onCopied = []

    protected void copy(File to, Predicate<File> toFilter, File from, Predicate<File> fromFilter ){
        if( to==null )throw new IllegalArgumentException( "to==null" );
        if( from==null )throw new IllegalArgumentException( "from==null" );

        if( !to.isAbsolute() )throw new IllegalArgumentException( "to is releative" );
        if( !from.isAbsolute() )throw new IllegalArgumentException( "from is releative" );

        if( fromFilter!=null && !fromFilter.test(from) )return;
        if( toFilter!=null && !toFilter.test(to) )return;

        if( from.isDir() ){
            if( !to.exists() ){
                to.createDirectories();
            }
            from.files.each {sfile ->
                copy(to.resolve(sfile.name), toFilter, sfile, fromFilter)
            }
        }else if( from.isFile() ){
            File dir = to.parent
            if( dir!=null && !dir.exists() ){
                dir.createDirectories()
            }

            try {
                long t0 = System.currentTimeMillis()
                long csize = -1
                try (
                    def fromStrm = from.readStream();
                    def toStrm = to.writeStream()
                ) {
                    csize = IOFun.copy(fromStrm, toStrm)
                }
                long t1 = System.currentTimeMillis()
                onCopied.each {ls -> ls(from,to,csize,t1-t0) }
            } catch ( IOException err ){
                if( onError.isEmpty() ) throw err;
                onError.each {it(err)}
            }
        }
    }

    @Override
    void run() {
        File from1 = from
        File to1 = to
        if( from1==null )throw new IllegalStateException("from==null")
        if( to1==null )throw new IllegalStateException("to==null")
        copy(to1, null, from1, null)
    }
}
