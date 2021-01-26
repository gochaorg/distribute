package xyz.cofe.dist.layout

import xyz.cofe.io.fs.File
import xyz.cofe.iter.Eterable

class TreeFileSet extends FileSet {
    TreeFileSet(File root, Eterable<File> files) {
        super(files)
        this.root = root
    }

    protected final File root;
    public File getRoot(){ return root; }
}
