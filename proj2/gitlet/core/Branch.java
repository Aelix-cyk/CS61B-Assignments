package gitlet.core;

/** Represents a named branch reference in the Gitlet system.
 *  Immutable value object.
 *  @author Aelix
 */
public class Branch {

    /** The branch name (e.g., "master", "feature-x"). */
    private final String name;

    /** The SHA-1 id of the tip commit. */
    private final String commitId;

    /** Creates a new branch reference. */
    public Branch(String name, String commitId) {
        this.name = name;
        this.commitId = commitId;
    }

    /** Returns the branch name. */
    public String name() {
        return name;
    }

    /** Returns the tip commit SHA-1 id. */
    public String commitId() {
        return commitId;
    }
}
