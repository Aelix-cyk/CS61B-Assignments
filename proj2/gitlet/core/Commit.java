package gitlet.core;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/** Represents a commit object in the Gitlet system.
 *  Pure domain object — persistence is handled by the storage layer.
 *  @author Aelix
 */
public class Commit implements Serializable {

    /** The commit message. */
    private String message;

    /** The date and time when the commit was created. */
    private ZonedDateTime timestamp;

    /** The SHA-1 id of the parent commit (null for initial commit). */
    private String parent;

    /** The SHA-1 id of the second parent (null for non-merge commits). */
    private String secondParent;

    /** Mapping from filename to blob SHA-1 id. */
    private HashMap<String, String> trackedFiles;

    /** Creates a commit with the given message, no parents, empty trackedFiles. */
    public Commit(String message) {
        this.message = message;
        timestamp = ZonedDateTime.now();
        trackedFiles = new HashMap<>();
    }

    /** Creates a commit that inherits trackedFiles from parentCommit. */
    public Commit(String message, Commit parentCommit, String parentId) {
        this.message = message;
        timestamp = ZonedDateTime.now();
        trackedFiles = new HashMap<>(parentCommit.trackedFiles);
        parent = parentId;
    }

    /** Set timestamp to Unix epoch. Used for the initial commit. */
    public void setEpochTime() {
        timestamp = Instant.EPOCH.atZone(ZoneId.systemDefault());
    }

    /** Set the second parent id (for merge commits). */
    public void setSecondParent(String id) {
        secondParent = id;
    }

    /** Returns the parent commit SHA-1 id. */
    public String getParent() {
        return parent;
    }

    /** Returns the second parent SHA-1 id (merge parent). */
    public String getSecondParent() {
        return secondParent;
    }

    /** Returns the commit message. */
    public String getMessage() {
        return message;
    }

    /** Returns the tracked files map (filename → blob SHA-1). */
    public Map<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    /** Returns true if this commit tracks a file with the given name. */
    public boolean hasFile(String name) {
        return trackedFiles.containsKey(name);
    }

    /** Returns true if this commit tracks a file with the given name and blob id. */
    public boolean hasSameFile(String name, String id) {
        return trackedFiles.containsKey(name) && trackedFiles.get(name).equals(id);
    }

    /** Add all entries from the given map to trackedFiles. */
    public void addFromMap(Map<String, String> addMap) {
        trackedFiles.putAll(addMap);
    }

    /** Remove all files in the given set from trackedFiles. */
    public void removeFromSet(Set<String> set) {
        for (String name : set) {
            trackedFiles.remove(name);
        }
    }

    /** Return a formatted log entry string for this commit. */
    public String dumpLog(String id) {
        String log = "===\n" + "commit " + id + "\n";
        if (secondParent != null) {
            log += "Merge: " + parent.substring(0, 7) + " " + secondParent.substring(0, 7) + "\n";
        }
        log += "Date: " + timestamp.format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy xx"))
                + "\n" + message + "\n\n";
        return log;
    }

    /** Returns the blob SHA-1 id for the given file. */
    public String trackedFileId(String file) {
        return trackedFiles.get(file);
    }
}
