package xyz.cofe.dist.layout

import xyz.cofe.io.fn.IOFun
import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileAttribute
import java.util.regex.Matcher
import java.util.regex.Pattern

class DirOp {
    public DirOp( File base, DirOp parent=null ){
        if( base==null )throw new IllegalArgumentException( "base==null" );
        this.base = base;
        this.parent = parent;
        if( parent!=null ){
            parent.children.add(this)
        }
    }

    //region id : String
    private String id = null;
    public String getId(){ this.@id }
    public void setId(String id){
        this.@id = id
    }
    //endregion

    //region cd()
    @SuppressWarnings('GrMethodMayBeStatic')
    public DirOp cd( Map opts, File dir, DirOp parent=null ){
        if( opts==null )opts = [:]
        if( dir==null )throw new IllegalArgumentException( "dir==null" );
        new DirOp(dir,this)
    }
    //endregion

    //region parent/children
    protected DirOp parent = null;
    public DirOp getParent(){ parent }

    protected final List<DirOp> children = [];
    public List<DirOp> getChildren(){ return this.@children }

    protected final List<Runnable> postRunJobs = []
    public List<Runnable> getPostRunJobs(){ this.@postRunJobs }

    public DirOp getRootDirOp(){
        DirOp dop = this
        while (dop.parent!=null){
            dop = dop.parent
        }
        return dop;
    }
    //endregion

    //region base : File
    private File base
    public File getBase(){ return this.base; }
    protected void setBase(File newBase){
        if( newBase==null )throw new IllegalArgumentException( "newBase==null" );
        this.base = newBase;
    }
    //endregion

    //region dir()
    DirOp dir( Map opts, File dir, Closure content=null ){
        if( opts==null )opts = [:]
        if( dir==null )throw new IllegalArgumentException( "dir==null" );
        if( !dir.isAbsolute() ){
            dir = base.resolve(dir)
        }
        def subDirProcess = { DirOp subDir ->
            if( opts.id != null ){
                subDir.id = opts.id as String
            }
        }
        if( !dir.exists ){
            dir.createDirectories();
            def subDir = cd(opts,dir)
            subDirProcess(subDir)
            if( content!=null ){
                content.delegate = subDir
                content.resolveStrategy = Closure.DELEGATE_FIRST
                content(subDir)
            }
            return subDir
        }else{
            if( dir.isDir() ){
                def subDir = cd(opts,dir)
                subDirProcess(subDir)
                if( content!=null ) {
                    content.delegate = subDir
                    content.resolveStrategy = Closure.DELEGATE_FIRST
                    content(subDir)
                }
                return subDir
            }else{
                throw new Error("can't mkdirs "+dir)
            }
        }
    }
    def dir( File dir, Closure content=null ){
        this.dir( [:], dir, content )
    }
    def dir( String dirname, @DelegatesTo(DirOp) Closure content=null ){
        if( dirname==null )throw new IllegalArgumentException( "dirname==null" );
        dir(new File(dirname),content)
    }
    def dir( Map opts, String dirname, @DelegatesTo(DirOp) Closure content=null ){
        if( dirname==null )throw new IllegalArgumentException( "dirname==null" );
        dir( opts ?: [:], new File(dirname),content)
    }
    //endregion

    //region clear()
    def clear(){
        if( base.isDir() ){
            base.getFiles().each {remove(it) }
        }
    }
    //endregion

    //region remove()
    def remove( File file, boolean recursive=true ){
        if( file==null )throw new IllegalArgumentException( "file==null" );
        if( file.isFile() ){
            file.delete()
        }else if( file.isDir() ){
            if( recursive ) {
                for (File f : file.files) {
                    remove(f)
                }
            }
            file.delete()
        }
    }
    def remove( String filename, boolean recursive=true ){
        if( filename!=null )throw new IllegalArgumentException( "filename!=null" );
        remove(new File(filename),recursive)
    }
    //endregion

    //region copy() / copy files
    def copy(File target, File from){
        if( target==null )throw new IllegalArgumentException( "target==null" );
        if( !target.isAbsolute() ){
            target = base.toAbsolute().resolve(target)
        }
        if( from==null )throw new IllegalArgumentException( "from==null" );
        if( from.isDir() ){
            if( !target.exists() ){
                target.createDirectories();
            }
            from.files.each {sfile ->
                copy(target.resolve(sfile.name), sfile)
            }
        }else if( from.isFile() ){
            File dir = target.parent
            if( dir!=null && !dir.exists() ){
                dir.createDirectories()
            }

            try(
                def fromStrm = from.readStream();
                def toStrm = target.writeStream()
            ){
                IOFun.copy(fromStrm,toStrm)
            }
        }
    }
    def copy(String filename, File from){
        if( filename==null )throw new IllegalArgumentException( "filename==null" );
        if( from==null )throw new IllegalArgumentException( "from==null" );
        File trgt = new File(filename)
        if( !trgt.isAbsolute() ){
            trgt = base.toAbsolute().resolve(trgt)
        }
        copy(trgt, from)
    }
    def copy(File from){
        if( from==null )throw new IllegalArgumentException( "from==null" );
        copy(base.toAbsolute().resolve(from.name), from)
    }
    def copy(String filename){
        if( filename==null )throw new IllegalArgumentException( "filename==null" );
        copy(new File(filename))
    }
    def copy(File target, InputStream stream){
        if( target==null )throw new IllegalArgumentException( "target==null" );
        if( !target.isAbsolute() ){
            target = base.resolve(target)
        }

        if( stream==null )throw new IllegalArgumentException( "stream==null" );

        File dir = target.parent
        if( dir!=null && !dir.exists ){
            dir.createDirectories()
        }

        try( def outstrm = target.writeStream() ){
            IOFun.copy( stream, outstrm )
        }
    }
    def copy(Iterable<File> files, File root=null){
        if( files==null )throw new IllegalArgumentException( "files==null" );
        if( root==null ){
            root = new File(".").toAbsolute().normalize()
        }
        for( File sfile : files ){
            File absoluteFile = sfile.isAbsolute() ? sfile : root.resolve(sfile)
            File relativeFile = root.relativize(absoluteFile)
            File target = base.resolve(relativeFile)
            copy( target, absoluteFile )
        }
    }
    //endregion

    //region chmod()
    Chmod getChmod(){
        return new Chmod().configure( c -> c.files = Eterable.single(base) )
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    Chmod chmod( FileSet fset ){
        if( fset==null )throw new IllegalArgumentException( "fset==null" );
        new Chmod().configure( c -> c.files = fset )
    }

    private static final Pattern grantPattern = Pattern.compile(
        /(?is)(?<target>[augo]+)(?<do>[+\-])(?<perm>[rwx]+)/
    )
    void chmod( FileSet fset, String grants ){
        if( fset==null )throw new IllegalArgumentException( "fset==null" );
        if( grants==null )throw new IllegalArgumentException( "grants==null" );
        Matcher m = grantPattern.matcher(grants)
        if( m.matches() ){
            String trgt = m.group('target').toLowerCase()
            String perm = m.group('perm').toLowerCase()
            Chmod ch = new Chmod()
            ch.files = fset
            def conf1 = { Chmod.PosixPerm p ->
                p.owner(trgt.contains('u') || trgt.contains('a'))
                p.group(trgt.contains('g') || trgt.contains('a'))
                p.others(trgt.contains('o') || trgt.contains('a'))

                p.read(perm.contains('r'))
                p.write(perm.contains('w'))
                p.execute(perm.contains('x'))
            }
            def a = m.group('do').contains('+') ? ch.append : ch.remove;
            conf1(a)
            a.run()
            return;
        }
        throw new IllegalArgumentException("grants not match pattern")
    }
    //endregion

    //region tree()
    FileSet tree(boolean relative){
        if( relative ) {
            File abase = base.toAbsolute().normalize()
            FileSet.of(abase.walk().go().map(f -> abase.relativize(f) ))
        }else{
            File abase = base.toAbsolute().normalize()
            FileSet.of(abase.walk().go().map(f -> f.toAbsolute().normalize() ))
        }
    }
    FileSet tree() { tree(false) }
    FileSet getTree() {
        tree(false)
    }
    //endregion

    //region text()
    def text( String filename, Closure content=null ){
        text([:],filename,content)
    }
    def text( Map opts, String filename, Closure content=null ){
        if( filename==null )throw new IllegalArgumentException( "filename==null" );

        def csOpt = opts?.charset ?: Charset.defaultCharset()
        Charset cs = csOpt instanceof Charset ? csOpt as Charset : Charset.forName(csOpt.toString())

        File targetFile = new File(filename)
        if( !targetFile.isAbsolute() ){
            targetFile = base.resolve(targetFile)
        }

        File dir = targetFile.parent
        if( dir!=null && !dir.exists ){
            dir.createDirectories()
        }

        boolean append = (opts?.append ?: false)

        try ( OutputStream strm = (append ? targetFile.writeStream( StandardOpenOption.APPEND ) : targetFile.writeStream()) ){
            try (OutputStreamWriter strmw = new OutputStreamWriter(strm, cs) ){
                try( PrintWriter pw = new PrintWriter(strmw) ){
                    content(pw)
                }
            }
        }
    }
    //endregion

    //region batch()
    def batch( String filename, @DelegatesTo(BatchOp) Closure batchConf ){
        if( filename==null )throw new IllegalArgumentException( "filename==null" );
        if( batchConf==null )throw new IllegalArgumentException( "batchConf==null" );

        BatchOp bop = new BatchOp()
        bop.dirOp = this
        bop.filename = filename

        getRootDirOp().postRunJobs.add(bop)

        batchConf.delegate = bop
        batchConf.resolveStrategy = Closure.DELEGATE_FIRST
        batchConf()
    }
    //endregion

    //region bash()
    def bash( String filename, @DelegatesTo(BashOp) Closure bashConf ){
        if( filename==null )throw new IllegalArgumentException( "filename==null" );
        if( bashConf==null )throw new IllegalArgumentException( "bashConf==null" );

        BashOp bop = new BashOp()
        bop.dirOp = this
        bop.filename = filename

        getRootDirOp().postRunJobs.add(bop)

        bashConf.delegate = bop
        bashConf.resolveStrategy = Closure.DELEGATE_FIRST
        bashConf()
    }
    //endregion

    def program( String basename, @DelegatesTo(ProgramOp) Closure conf ){
        if( basename==null )throw new IllegalArgumentException( "basename==null" );
        if( conf==null )throw new IllegalArgumentException( "conf==null" );

        ProgramOp task = new ProgramOp()
        task.dirOp = this
        task.basename = basename

        getRootDirOp().postRunJobs.add(task)

        conf.delegate = task
        conf.resolveStrategy = Closure.DELEGATE_FIRST
        conf()
    }
}
