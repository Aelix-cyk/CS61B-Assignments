package gitlet;

import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

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

    Commit(String message, Commit parentCommit, String parentId) {
        this.message = message;
        timestamp = ZonedDateTime.now();
        trackedFiles = new HashMap<>(parentCommit.trackedFiles);
        parent = parentId;
    }

    /** Set timestamp with epoch time */
    public void setEpochTime() {
        timestamp = Instant.EPOCH.atZone(ZoneId.systemDefault());
    }

    public void setSecondParent(String id) {
        secondParent = id;
    }

    public String getParent() {
        return parent;
    }

    /** Write commit object into file */
    public static String saveCommit(Commit commit) {
        byte[] contents = serialize(commit);
        String id = sha1(contents);
        writeContents(join(COMMITS_DIR, id), contents);
        return id;
    }

    /** Load commit object from file */
    public static Commit fromFile(File file) {
        return readObject(file, Commit.class);
    }

    /** Check if this commit has the file */
    public boolean hasFile(String name) {
        return trackedFiles.containsKey(name);
    }

    /** Check if this commit has the same file */
    public boolean hasSameFile(String name, String id) {
        return trackedFiles.containsKey(name) && trackedFiles.get(name).equals(id);
    }

    /** Add files from map to trackedFiles */
    public void addFromMap(Map<String, String> addMap) {
        trackedFiles.putAll(addMap);
    }

    /** Remove files in Set from trackedFiles */
    public void removeFromSet(Set<String> set) {
        for (String name : set) {
            trackedFiles.remove(name);
        }
    }

    /** Return a string that contains the commit log */
    public String dumpLog(String id) {
        String log = "===\n" + "commit " + id + "\n";
        if (secondParent != null) {
            log += "Merge: " + parent.substring(0, 5) + " " + secondParent.substring(0, 5) + "\n";
        }
        log += "Date: " + timestamp.format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy XX")) + "\n" +
                message + "\n\n";
        return log;
    }

}
