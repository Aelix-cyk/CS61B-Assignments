package gitlet.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitlet.Utils.*;
import static gitlet.util.Paths.*;

/** Manages branch references and HEAD in .gitlet/.
 *  All data is stored as plain text files (not serialized objects).
 *  @author Aelix
 */
public class RefStore {

    /* ==================== HEAD Operations ==================== */

    /** Returns the HEAD reference string (e.g., "refs/master").
     *  Returns null if HEAD_FILE does not exist.
     */
    public String getHeadRef() {
        if (!HEAD_FILE.exists()) {
            return null;
        }
        return readContentsAsString(HEAD_FILE);
    }

    /** Set HEAD to point to the given branch name. */
    public void setHeadRef(String branchName) {
        writeContents(HEAD_FILE, REFS_DIR.getName() + "/" + branchName);
    }

    /** Returns the name of the currently checked-out branch. */
    public String getCurrentBranchName() {
        Pattern pattern = Pattern.compile("refs/(?<branchName>\\w+)");
        Matcher matcher = pattern.matcher(getHeadRef());
        if (matcher.find()) {
            return matcher.group("branchName");
        } else {
            message("Failed to get Head's branch name");
            System.exit(0);
            return "";
        }
    }

    /* ==================== Branch Operations ==================== */

    /** Returns the tip commit SHA-1 for the given branch. */
    public String getBranchCommitId(String branchName) {
        return readContentsAsString(join(REFS_DIR, branchName));
    }

    /** Update the tip commit SHA-1 for the active branch. */
    public void setBranchCommitId(String branchName, String commitId) {
        writeContents(join(REFS_DIR, branchName), commitId);
    }

    /** Create a new branch pointing to the given commit.
     *  Fails if a branch with the same name already exists.
     */
    public void createBranch(String branchName, String commitId) {
        File file = join(REFS_DIR, branchName);
        if (fileExists(file)) {
            message("A branch with that name already exists.");
            System.exit(0);
        } else {
            writeContents(file, commitId);
        }
    }

    /** Delete a branch. Fails if the branch does not exist or is the current branch. */
    public void deleteBranch(String branchName) {
        File file = join(REFS_DIR, branchName);
        if (!fileExists(file)) {
            message("A branch with that name does not exist.");
        } else if (branchName.equals(getCurrentBranchName())) {
            message("Cannot remove the current branch.");
        } else if (file.delete()) {
            return;
        }
        System.exit(0);
    }

    /** Returns true if a branch with the given name exists. */
    public boolean branchExists(String branchName) {
        return fileExists(join(REFS_DIR, branchName));
    }

    /** Returns the names of all branches. */
    public List<String> allBranchNames() {
        return new ArrayList<>(Objects.requireNonNull(plainFilenamesIn(REFS_DIR)));
    }
}
