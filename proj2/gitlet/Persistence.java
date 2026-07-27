package gitlet;

import java.util.List;

import static gitlet.Paths.*;
import static gitlet.Utils.*;

public class Persistence {

    /** Write commit object into file */
    public static String saveCommit(Commit commit) {
        byte[] contents = serialize(commit);
        String id = sha1((Object) contents);
        writeContents(join(COMMITS_DIR, id), (Object) contents);
        return id;
    }

    /** Load commit object according to id */
    public static Commit loadCommit(String id) {
        return readObject(join(COMMITS_DIR, id), Commit.class);
    }

    /** Write stage object into file */
    public static void saveIndex(Index index) {
        writeObject(INDEX_FILE, index);
    }

    /** Read stage object from file */
    public static Index loadIndex() {
        return readObject(INDEX_FILE, Index.class);
    }

    /** Write blob into file */
    public static void saveBlob(Blob blob) {
        writeContents(join(BLOBS_DIR, blob.getId()), (Object) blob.getContents());
    }

    /** Load blob from file */
    public static Blob loadBlob(String id) {
        return new Blob(readContents(join(BLOBS_DIR, id)));
    }

    /** Restore given file from blobs by sha-1 id */
    public static void restoreFile(String file, String id) {
        writeContents(join(CWD, file), (Object) readContents(join(BLOBS_DIR, id)));
    }

    /** Return the long id with its first 6 or more digits if it exists */
    public static String getLongId(String id) {
        List<String> files = plainFilenamesIn(COMMITS_DIR);
        if (files == null) {
            message("Unexpected error: no commit in COMMITS_DIR!");
            System.exit(0);
        }
        if (files.contains(id)) {
            return id;
        }
        for (String file : files) {
            if (file.startsWith(id)) {
                return file;
            }
        }
        message("No commit with that id exists.");
        System.exit(0);
        return "";
    }

    /** Initialize .gitlet */
    public static void init() {
        boolean result;
        result = GITLET_DIR.mkdir() | BLOBS_DIR.mkdir() | COMMITS_DIR.mkdir() | REFS_DIR.mkdir();
        if (!result) {
            message("setupPersistence failed!");
            System.exit(0);
        }
        saveIndex(new Index());
    }

}
