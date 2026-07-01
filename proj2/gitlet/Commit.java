package gitlet;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.Instant;
import java.util.HashMap;
import java.io.Serializable;

/** Represents a gitlet commit object.
 *  It represents a commit that would be stored in a file.
 *  @author TODO
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

}
