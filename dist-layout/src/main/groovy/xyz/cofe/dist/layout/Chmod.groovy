package xyz.cofe.dist.layout

import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

import java.nio.file.attribute.PosixFilePermission
import java.util.function.Consumer
import java.util.function.Predicate

class Chmod {
    Chmod(){}
    Chmod configure(Consumer<Chmod> conf){
        if( conf==null )throw new IllegalArgumentException( "conf==null" );
        conf.accept(this)
        return this
    }

    Eterable<File> files

    public abstract class PosixPerm<SELF> {
        boolean owner;
        SELF owner(boolean v){
            owner = v
            this
        }

        boolean group;
        SELF group(boolean v){
            group = v
            this
        }

        boolean others;
        SELF others(boolean v){
            others = v
            this
        }

        SELF to( boolean toOwner, boolean toGroup=false, boolean toOthers=false ){
            owner = toOwner
            group = toGroup
            others = toOthers
            this
        }

        boolean read;
        SELF read(boolean grant){
            read = grant
            this
        }

        boolean write;
        SELF write( boolean grant ){
            write = grant
            this
        }

        boolean execute;
        SELF execute( boolean grant ){
            execute = grant
            this
        }

        SELF perms( boolean read, boolean write, boolean execute ){
            this.read = read
            this.write = write
            this.execute = execute
            this
        }
    }

    class Append extends PosixPerm<Append> implements Runnable {
        void run(){
            Set<PosixFilePermission> appends = new LinkedHashSet<>()
            if( owner && read ) appends.add( PosixFilePermission.OWNER_READ )
            if( owner && write ) appends.add( PosixFilePermission.OWNER_WRITE )
            if( owner && execute ) appends.add( PosixFilePermission.OWNER_EXECUTE )
            if( group && read ) appends.add( PosixFilePermission.GROUP_READ )
            if( group && write ) appends.add( PosixFilePermission.GROUP_WRITE )
            if( group && execute ) appends.add( PosixFilePermission.GROUP_EXECUTE )
            if( others && read ) appends.add( PosixFilePermission.OTHERS_READ )
            if( others && write ) appends.add( PosixFilePermission.OTHERS_WRITE )
            if( others && execute ) appends.add( PosixFilePermission.OTHERS_EXECUTE )

            for( File file in files ){
                if( file.exists ){
                    def posixPerm = file.getPosixFilePermissions()
                    def newPerms = new LinkedHashSet()
                    newPerms.addAll(posixPerm)
                    newPerms.addAll(appends)
                    try {
                        file.setPosixFilePermissions(newPerms)
                    } catch( Throwable err ){
                        System.err.println("can't set posix perms to $file")
                    }
                }
            }
        }
    }

    class Revoke extends PosixPerm<Revoke> implements Runnable {
        private boolean running = false

        private List<Predicate<PosixFilePermission>> permsFiltersCache = null
        private List<Predicate<PosixFilePermission>> getPermsFilters(){
            if( running && permsFiltersCache!=null )return permsFiltersCache

            def res = []
            if( owner && read ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.OWNER_READ) as Predicate<PosixFilePermission> )
            if( owner && write ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.OWNER_WRITE) as Predicate<PosixFilePermission> )
            if( owner && execute ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.OWNER_EXECUTE) as Predicate<PosixFilePermission> )

            if( group && read ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.GROUP_READ) as Predicate<PosixFilePermission> )
            if( group && write ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.GROUP_WRITE) as Predicate<PosixFilePermission> )
            if( group && execute ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.GROUP_EXECUTE) as Predicate<PosixFilePermission> )

            if( others && read ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.OTHERS_READ) as Predicate<PosixFilePermission> )
            if( others && write ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.OTHERS_WRITE) as Predicate<PosixFilePermission> )
            if( others && execute ) res.add( ((PosixFilePermission p) -> p == PosixFilePermission.OTHERS_EXECUTE) as Predicate<PosixFilePermission> )

            permsFiltersCache = res
            return res
        }

        private boolean matchAll( PosixFilePermission p ){
            if( p==null )throw new IllegalArgumentException( "p==null" );
            for( def flt : permsFilters ){
                if( !flt.test(p) )return false
            }
            return true
        }

        private boolean matchAny( PosixFilePermission p ){
            if( p==null )throw new IllegalArgumentException( "p==null" );
            for( def flt : permsFilters ){
                if( flt.test(p) )return true
            }
            return false
        }

        Set<PosixFilePermission> remove( Set<PosixFilePermission> perms ){
            if( perms==null )throw new IllegalArgumentException( "perms==null" );

            LinkedHashSet<PosixFilePermission> newperms = new LinkedHashSet<>();
            for( PosixFilePermission p : perms ){
                if( !matchAny(p) ) {
                    newperms.add(p)
                }
            }

            return newperms
        }

        @Override
        void run() {
            try {
                running = true

                for( File file in files ){
                    if( file.exists ){
                        def posixPerm = file.getPosixFilePermissions()
                        def newPerms = remove(posixPerm)
                        try {
                            file.setPosixFilePermissions(newPerms)
                        } catch( Throwable err ){
                            System.err.println("can't set posix perms to $file")
                        }
                    }
                }
            } finally {
                running = false
            }
        }
    }

    Append getAppend(){
        return new Append()
    }

    Revoke getRemove(){
        return new Revoke()
    }
}
