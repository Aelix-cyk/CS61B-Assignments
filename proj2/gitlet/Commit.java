package gitlet;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/** Represents a gitlet commit object.
 *  It represents a commit that would be stored in a file.
 *  @author Aelix
 */
public class Commit implements Serializable {

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

    public String getSecondParent() {
        return secondParent;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getTrackedFiles() {
        return trackedFiles;
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
        StringBuilder log = new StringBuilder();
        log.append("===\n").append("commit ").append(id).append("\n");
        if (secondParent != null) {
            log.append("Merge: ").append(parent, 0, 7).append(" ");
            log.append(secondParent, 0, 7).append("\n");
        }
        log.append("Date: ").append(timestamp.format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy xx")));
        log.append("\n").append(message).append("\n\n");
        return log.toString();
    }

    /** Return the sha-1 id of file from trackedFiles */
    public String trackedFileId(String file) {
        return trackedFiles.get(file);
    }

}
