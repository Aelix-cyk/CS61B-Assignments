package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Represents a gitlet index object.
 *  It stores data about staging area.
 */
public class Index implements Serializable, Dumpable {


    /** Map for files that are staging for addition */
    private HashMap<String, String> additionMap;
    /** Map for files that are staging for removal */
    private HashSet<String> removalSet;

    Index() {
        additionMap = new HashMap<>();
        removalSet = new HashSet<>();
    }

    /** Check if additionMap has a file with same name and sha-1 id */
    public boolean hasSameFile(String name, String id) {
        return additionMap.containsKey(name) && additionMap.get(name).equals(id);
    }

    /** Check if additionMap has a file with same name */
    public boolean hasFile(String name) {
        return additionMap.containsKey(name);
    }

    /** Add file to staging area for addition */
    public void stageForAddition(String fileName, String blobId) {
        additionMap.put(fileName, blobId);
        removalSet.remove(fileName);
    }

    /** Remove file from staging area for addition. Add it to staging area for removal */
    public void stageForRemoval(String fileName) {
        additionMap.remove(fileName);
        removalSet.add(fileName);
    }

    /** Remove file from stageing area */
    public void unstage(String fileName) {
        additionMap.remove(fileName);
        removalSet.remove(fileName);
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
        for (String file : additionMap.keySet()) {
            System.out.print(file + ", ");
        }
        System.out.println("\nremovalSet:");
        for (String file : removalSet) {
            System.out.print(file + ", ");
        }
    }
}
