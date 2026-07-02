package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Utils.*;

/** Represents a gitlet stage object.
 *  It stores data about staging area.
 */
public class Stage implements Serializable {

    /** The file that stores stage object */
    public static final File STAGE_FILE = join(Repository.GITLET_DIR, "STAGE");

    /** Map for files that are staging for addition */
    private HashMap<String, String> additionMap;
    /** Map for files that are staging for removal */
    private HashMap<String, String> removalMap;

    Stage () {
        additionMap = new HashMap<>();
        removalMap = new HashMap<>();
    }

    /** Write stage object into file */
    public static void saveStage(Stage stage) {
        writeObject(STAGE_FILE, stage);
    }

    /** Read stage object from file */
    public static Stage fromFile() {
        return readObject(STAGE_FILE, Stage.class);
    }

    public boolean hasSameFile(String name, String id) {
        return additionMap.containsKey(name) && additionMap.get(name).equals(id);
    }

    /** Add file to staging area */
    public void addToStage(File file, Commit commit) {
        if (!fileExists(file)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        String id = sha1(readContents(file));
        String name = file.getName();

        if (!commit.hasSameFile(name ,id)) {
            if (!this.hasSameFile(name, id)) {
                additionMap.put(name, id);
            }
        } else {
            if (additionMap.containsKey(name)) {
                additionMap.remove(name);
            }
        }
        if (removalMap.containsKey(name)) {
            removalMap.remove(name);
        }
    }
}
