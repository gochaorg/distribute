package xyz.cofe.dist.layout

import xyz.cofe.io.fs.File

import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.function.Function

class PrintFiles {
    PrintFiles(){}
    PrintFiles( PrintFiles sample ){
        if( sample==null )throw new IllegalArgumentException( "sample==null" );
        files = sample.files
        resolver = sample.resolver
        olderDateFormat = sample.olderDateFormat
        currentDateFormat = sample.currentDateFormat
    }

    PrintFiles configure( Consumer<PrintFiles> conf ){
        if( conf==null )throw new IllegalArgumentException( "conf==null" );
        conf.accept(this)
        this
    }

    Iterable<File> files;

    Function<File,File> resolver;
    protected File resolve( File file ){
        if( file==null )return file
        if( file.isAbsolute() )return file

        Function<File,File> resl = resolver
        if( resl==null )throw new IllegalStateException("can't resolve $file")

        resl.apply(file)
    }

    protected DateTimeFormatter olderDateFormat = DateTimeFormatter.ofPattern('yyyy-MM-dd')
    protected DateTimeFormatter currentDateFormat = DateTimeFormatter.ofPattern('HH:mm:ss')

    protected String rwx( File file ){
        StringBuilder sb = new StringBuilder();
        File f = resolve(file)

        if( f.isDirectory() ){
            sb.append("d")
        }else if( f.isRegularFile() ){
            sb.append("-")
        }else if( f.isSymbolicLink() ){
            sb.append("l")
        }else{
            sb.append("?")
        }

        Set<PosixFilePermission> perms = f.getPosixFilePermissions()
        if( perms!=null ){
            sb.append( perms.contains(PosixFilePermission.OWNER_READ) ? "r" : "-" )
            sb.append( perms.contains(PosixFilePermission.OWNER_WRITE) ? "w" : "-" )
            sb.append( perms.contains(PosixFilePermission.OWNER_EXECUTE) ? "x" : "-" )
            sb.append( perms.contains(PosixFilePermission.GROUP_READ) ? "r" : "-" )
            sb.append( perms.contains(PosixFilePermission.GROUP_WRITE) ? "w" : "-" )
            sb.append( perms.contains(PosixFilePermission.GROUP_EXECUTE) ? "x" : "-" )
            sb.append( perms.contains(PosixFilePermission.OTHERS_READ) ? "r" : "-" )
            sb.append( perms.contains(PosixFilePermission.OTHERS_WRITE) ? "w" : "-" )
            sb.append( perms.contains(PosixFilePermission.OTHERS_EXECUTE) ? "x" : "-" )
        }else{
            sb.append("?????????")
        }
        return sb.toString()
    }
    protected String fsize( long size ){
        if( size<1024 )return Long.toString(size);
        if( size<1024**2 )return ((size/1024) as long)+"k";
        if( size<1024**3 )return ((size/1024/1024) as long)+"m";
        return ((size/1024/1024/1024) as long)+"g";
    }

    void print( Appendable out ) {
        if( out==null )throw new IllegalArgumentException( "out==null" );

        Date curDay = new Date()

        GregorianCalendar gcal = new GregorianCalendar()
        gcal.setTime(curDay)
        gcal.set(GregorianCalendar.MILLISECOND,0)
        gcal.set(GregorianCalendar.SECOND,0)
        gcal.set(GregorianCalendar.MINUTE,0)
        gcal.set(GregorianCalendar.HOUR_OF_DAY,0)
        curDay = gcal.getTime()

        for( File file : files ){
            List<String> fieldValues = []

            File f = resolve(file)

            fieldValues += rwx(file)

            def own = f.getOwner()
            if( own!=null ){
                fieldValues += own.toString()
            }else {
                fieldValues += '?'
            }

            PosixFileAttributeView psx = f.getFileAttributeView(PosixFileAttributeView.class);
            if( psx!=null ){
                fieldValues += psx.readAttributes().group().toString()
            }else {
                fieldValues += "?"
            }

            Date d = f.getLastModifiedDate();
            DateTimeFormatter df = d.before(curDay) ? olderDateFormat : currentDateFormat

            if( f.isRegularFile() ) {
                fieldValues += fsize(f.getSize());
            }else{
                fieldValues += '?'
            }

            fieldValues += df.format(d.toInstant().atZone(ZoneId.systemDefault()));

            StringBuilder fname = new StringBuilder()
            if( file.toString().length()==0 ){
                fname << "."
            }

            fname << file.toString()
            if( f.isDir() ){
                fname << System.properties['file.separator']?.toString()
            }

            fieldValues += fname.toString()

            out.append(
                fieldValues.indices.collect {fi ->
                    if( fi in [1,2] ){
                        return fieldValues[fi].padLeft(8)
                    }else if( fi==3 ){
                        return fieldValues[fi].padLeft(5)
                    }else if( fi==4 ){
                        return fieldValues[fi].padLeft(10)
                    }
                    fieldValues[fi]
                }.join(' ')
            )
            out.append(System.lineSeparator());
        }
    }
}
