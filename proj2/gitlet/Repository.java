package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  It contains the main logic of gitlet.
 *  @author TODO
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The commits directory. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    /** The refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    public static void setupPersistence() {
        boolean result =GITLET_DIR.mkdir() | BLOBS_DIR.mkdir()
                | COMMITS_DIR.mkdir() | REFS_DIR.mkdir();
        if (!result) {
            System.out.println("setupPersistence failed!");
            System.exit(0);
        }
    }

    /** Check if there is a gitlet system already */
    public static boolean initialCheck() {
        return !directoryExists(GITLET_DIR.getName());
    }

    /** Check is there is a file with same name already */
    public static boolean fileExists(String name) {
        Path targetPath = Paths.get(name);
        return Files.exists(targetPath) && (!Files.isDirectory(targetPath));
    }

    /** Check is there is a file with same name already */
    public static boolean fileExists(File file) {
        Path targetPath = Paths.get(file.getName());
        return Files.exists(targetPath) && (!Files.isDirectory(targetPath));
    }

    /** Check is there is a directory with same name already */
    public static boolean directoryExists(String name) {
        Path targetPath = Paths.get(name);
        return Files.exists(targetPath) && Files.isDirectory(targetPath);
    }

    /** Initial commit */
    public static void initialize() {
        String defaultBranch = "master";
        Commit initialCommit = new Commit("initial commit");

        setupPersistence();
        initialCommit.setEpochTime();
        String id = saveCommit(initialCommit);
        createBranch(defaultBranch, id);
        setHead(defaultBranch);
    }

    /** Write commit object into file */
    public static String saveCommit(Commit commit) {
        byte[] contents = serialize(commit);
        String id = sha1(contents);
        writeContents(join(COMMITS_DIR, id), contents);
        return id;
    }

    /** Create a branch with given name */
    public static void createBranch(String branch, String id) {
        File file = join(REFS_DIR, branch);
        if (fileExists(file)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            writeContents(file, id);
        }
    }

    /** Set the head pointer */
    public static void setHead(String branch) {
        File file = join(GITLET_DIR, "HEAD");
        writeContents(file, REFS_DIR.getName() + "/" + branch);
    }

}
