package gitlet;

import java.io.File;

import static gitlet.Utils.join;

/**
 * This class contains the main paths of persistence files.
 */
public class Paths {
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File INDEX_FILE = join(GITLET_DIR, "INDEX");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

}
