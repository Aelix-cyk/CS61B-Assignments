package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet stage object.
 *  It stores data about staging area.
 */
public class Stage implements Serializable, Dumpable {

    /** The file that stores stage object */
    public static final File STAGE_FILE = join(Repository.GITLET_DIR, "STAGE");

    /** Map for files that are staging for addition */
    private HashMap<String, String> additionMap;
    /** Map for files that are staging for removal */
    private HashSet<String> removalSet;

    Stage () {
        additionMap = new HashMap<>();
        removalSet = new HashSet<>();
    }

    /** Write stage object into file */
    public static void saveStage(Stage stage) {
        writeObject(STAGE_FILE, stage);
    }

    /** Read stage object from file */
    public static Stage fromFile() {
        return readObject(STAGE_FILE, Stage.class);
    }

    /** Check if additionMap has a file with same name and sha-1 id */
    public boolean hasSameFile(String name, String id) {
        return additionMap.containsKey(name) && additionMap.get(name).equals(id);
    }

    /** Check if additionMap has a file with same name */
    public boolean hasFile(String name) {
        return additionMap.containsKey(name);
    }

    /** Add file to staging area */
    public void addFile(File file, Commit commit) {
        if (!fileExists(file)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        byte[] contents = readContents(file);
        String id = sha1(contents);
        String name = file.getName();

        if (!commit.hasSameFile(name ,id)) {
            // file that has new name or contents should be saved
            if (!this.hasSameFile(name, id)) {
                additionMap.put(name, id);
                Repository.saveBlob(id, contents);
            }
        } else {
            // file that is in additionMap and has same file in current commit should be removed
            if (additionMap.containsKey(name)) {
                additionMap.remove(name);
            }
        }

        // remove file from removalMap
        if (removalSet.contains(name)) {
            removalSet.remove(name);
        }
    }

    /** Remove file from staging area. Add it to stage for removal if it's in current commit. */
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
            /* remove file if it is in current commit */
            if (file.exists()) {
                restrictedDelete(file);
            }
        }
    }

    /** Return the additionMap */
    public Map<String, String> getAdditionMap() {
        return additionMap;
    }

    /** Return the removalSet */
    public Set<String> getRemovalSet() {
        return removalSet;
    }

    /** Clear staging area after commit */
    public void clear() {
        additionMap.clear();
        removalSet.clear();
    }

    /** Check additionMap and removalSet is empty or not */
    public boolean isEmpty() {
        return additionMap.isEmpty() && removalSet.isEmpty();
    }

    /** Dump information */
    @Override
    public void dump() {
        System.out.println("additionMap:");
        for (String file : additionMap.keySet()) System.out.print(file + ", ");
        System.out.println("\nremovalSet:");
        for (String file : removalSet) System.out.print(file + ", ");
    }
}
