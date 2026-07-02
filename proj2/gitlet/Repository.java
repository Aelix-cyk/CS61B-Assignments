package gitlet;

import java.io.File;
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
    /** The refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The head file */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    /** Setup files and directories for persistence */
    public static void setupPersistence() {
        boolean result;

        result = GITLET_DIR.mkdir()
                | BLOBS_DIR.mkdir()
                | Commit.COMMITS_DIR.mkdir()
                | REFS_DIR.mkdir();
        if (!result) {
            System.out.println("setupPersistence failed!");
            System.exit(0);
        }

        Stage.saveStage(new Stage());
    }

    /** Check if there is a gitlet system already */
    public static boolean initialCheck() {
        return !directoryExists(GITLET_DIR.getName());
    }

    /** Initial commit */
    public static void initialize() {
        String defaultBranch = "master";
        Commit initialCommit = new Commit("initial commit");

        setupPersistence();
        initialCommit.setEpochTime();
        String id = Commit.saveCommit(initialCommit);
        createBranch(defaultBranch, id);
        setHead(defaultBranch);
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
        writeContents(HEAD_FILE, REFS_DIR.getName() + "/" + branch);
    }

    /** Get the commit object that HEAD point to */
    public static Commit getHeadCommit() {
        String branch = readContentsAsString(HEAD_FILE);
        String id = readContentsAsString(join(GITLET_DIR, branch));
        return readObject(join(Commit.COMMITS_DIR, id), Commit.class);
    }

    /** Add file to staging area */
    public static void addToStage(String name) {
        Stage stage = Stage.fromFile();
        stage.addToStage(join(CWD, name), getHeadCommit());
        Stage.saveStage(stage);
    }

}
