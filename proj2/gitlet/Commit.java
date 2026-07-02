package gitlet;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.Instant;
import java.util.HashMap;
import java.io.Serializable;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  It represents a commit that would be stored in a file.
 *  @author TODO
 */
public class Commit implements Serializable {

    /** The commit directory. */
    public static final File COMMITS_DIR = join(Repository.GITLET_DIR, "commits");

    /** The message of this Commit. */
    private String message;
    /** The date and time when committing. */
    private ZonedDateTime timestamp;
    /** The string of its parent's SHA-1 id. */
    private String parent;
    /** The string of its second parent's SHA-1 id. */
    private String secondParent;
    /** The core map from file name to SHA-1 id. */
    private HashMap<String, String> trackedFiles;

    Commit(String message) {
        this.message = message;
        timestamp = ZonedDateTime.now();
        trackedFiles = new HashMap<>();
    }

    /** Set timestamp with epoch time */
    public void setEpochTime() {
        timestamp = Instant.EPOCH.atZone(ZoneOffset.UTC);
    }

    public void setParent(String id) {
        parent = id;
    }

    public void setSecondParent(String id) {
        secondParent = id;
    }

    public static String saveCommit(Commit commit) {
        byte[] contents = serialize(commit);
        String id = sha1(contents);
        writeContents(join(COMMITS_DIR, id), contents);
        return id;
    }

    /** Check if this commit has the same file */
    public boolean hasSameFile(String name, String id) {
        return trackedFiles.containsKey(name) && trackedFiles.get(name).equals(id);
    }

}
