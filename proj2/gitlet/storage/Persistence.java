package gitlet.storage;

import gitlet.core.Blob;
import gitlet.core.Commit;
import gitlet.core.Index;

import java.util.List;

import static gitlet.Utils.*;
import static gitlet.util.Paths.*;

/** Handles persistence for blobs, commits, and the index in .gitlet/.
 *  All disk I/O for these objects goes through this class.
 *  @author Aelix
 */
public class Persistence {

    /** Initialize the .gitlet/ directory structure and save an empty index.
     *  Called once during 'gitlet init'. Fails fatally if dirs already exist.
     */
    public static void init() {
        boolean result = GITLET_DIR.mkdir()
                | OBJECTS_DIR.mkdir()
                | COMMITS_DIR.mkdir()
                | REFS_DIR.mkdir();
        if (!result) {
            message("setupPersistence failed!");
            System.exit(0);
        }
        saveIndex(new Index());
    }

    /** Returns true if a .gitlet/ repository already exists. */
    public static boolean repoExists() {
        return GITLET_DIR.exists();
    }

    /* ==================== Blob Operations ==================== */

    /** Save a blob to .gitlet/objects/<id>. */
    public void saveBlob(Blob blob) {
        writeContents(join(OBJECTS_DIR, blob.id()), (Object) blob.contents());
    }

    /** Load a blob's raw bytes from .gitlet/objects/<id>. */
    public byte[] loadBlob(String blobId) {
        return readContents(join(OBJECTS_DIR, blobId));
    }

    /* ==================== Commit Operations ==================== */

    /** Serialize and persist a commit. Returns the SHA-1 id. */
    public String saveCommit(Commit commit) {
        byte[] contents = serialize(commit);
        String id = sha1((Object) contents);
        writeContents(join(COMMITS_DIR, id), (Object) contents);
        return id;
    }

    /** Load a commit from .gitlet/commits/<id>. */
    public Commit loadCommit(String commitId) {
        return readObject(join(COMMITS_DIR, commitId), Commit.class);
    }

    /** Returns all commit ids in the repository. */
    public List<String> allCommitIds() {
        return plainFilenamesIn(COMMITS_DIR);
    }

    /** Resolve an abbreviated commit id to its full 40-char SHA-1.
     *  If the id is already a complete match, return it.
     *  Otherwise find the first commit id starting with the given prefix.
     */
    public String resolveShortId(String shortId) {
        List<String> ids = plainFilenamesIn(COMMITS_DIR);
        if (ids != null && ids.contains(shortId)) {
            return shortId;
        }
        if (ids != null) {
            for (String id : ids) {
                if (id.startsWith(shortId)) {
                    return id;
                }
            }
        }
        message("No commit with that id exists.");
        System.exit(0);
        return "";
    }

    /* ==================== Index Operations ==================== */

    /** Load the staging area from .gitlet/STAGE. */
    public Index loadIndex() {
        return readObject(INDEX_FILE, Index.class);
    }

    /** Save the staging area to .gitlet/STAGE. */
    public static void saveIndex(Index index) {
        writeObject(INDEX_FILE, index);
    }
}
