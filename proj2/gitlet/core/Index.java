package gitlet.core;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents the staging area (index) in the Gitlet system.
 *  Tracks files staged for addition and removal.
 *  Persistence is handled by the storage layer.
 *  @author Aelix
 */
public class Index implements Serializable {

    /** Files staged for addition: filename → blob SHA-1. */
    private HashMap<String, String> additionMap;

    /** Files staged for removal. */
    private HashSet<String> removalSet;

    /** Creates an empty index. */
    public Index() {
        additionMap = new HashMap<>();
        removalSet = new HashSet<>();
    }

    /** Returns true if additionMap contains a file with the given name and blob id. */
    public boolean hasSameFile(String name, String id) {
        return additionMap.containsKey(name) && additionMap.get(name).equals(id);
    }

    /** Returns true if additionMap contains a file with the given name. */
    public boolean hasFile(String name) {
        return additionMap.containsKey(name);
    }

    /** Add a file to the staging area.
     *  Returns a Blob to be persisted, or null if no blob needs saving.
     */
    public Blob addFile(File file, Commit commit) {
        if (!fileExists(file)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        byte[] contents = readContents(file);
        String name = file.getName();

        /* File is identical to what the commit already tracks → unstage it */
        if (commit.hasSameFile(name, sha1((Object) contents))) {
            additionMap.remove(name);
            removalSet.remove(name);
            return null;
        }

        /* This exact version is not already staged → stage it and save blob */
        if (!this.hasSameFile(name, sha1((Object) contents))) {
            Blob blob = new Blob(contents);
            additionMap.put(name, blob.id());
            removalSet.remove(name);
            return blob;
        }

        /* Already staged with same content — ensure it's not in removalSet */
        removalSet.remove(name);
        return null;
    }

    /** Remove a file from the staging area.
     *  If the file is tracked by the current commit, stage it for removal
     *  and delete it from the working directory.
     */
    public void removeFile(File file, Commit commit) {
        String name = file.getName();
        boolean fileInAdditionMap = additionMap.containsKey(name);
        boolean fileInCommit = commit.hasFile(name);

        if (!fileInAdditionMap && !fileInCommit) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (fileInAdditionMap) {
            additionMap.remove(name);
        }

        if (fileInCommit) {
            removalSet.add(name);
            if (file.exists()) {
                restrictedDelete(file);
            }
        }
    }

    /** Returns the addition map (filename → blob SHA-1). */
    public Map<String, String> getAdditionMap() {
        return additionMap;
    }

    /** Returns the set of files staged for removal. */
    public Set<String> getRemovalSet() {
        return removalSet;
    }

    /** Clear all staged additions and removals. */
    public void clear() {
        additionMap.clear();
        removalSet.clear();
    }

    /** Returns true if no files are staged. */
    public boolean isEmpty() {
        return additionMap.isEmpty() && removalSet.isEmpty();
    }
}
