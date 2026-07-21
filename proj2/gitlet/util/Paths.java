package gitlet.util;

import java.io.File;

import static gitlet.Utils.join;

/** Centralized path constants for the .gitlet/ directory structure.
 *  @author Aelix
 */
public class Paths {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory containing all persistent state. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** Directory for blob objects (file snapshots), keyed by SHA-1. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "blobs");

    /** Directory for commit objects, keyed by SHA-1. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /** Directory for branch reference files. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    /** File containing the current HEAD reference (e.g., "refs/master"). */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    /** File containing the serialized Index (staging area). */
    public static final File INDEX_FILE = join(GITLET_DIR, "STAGE");
}
