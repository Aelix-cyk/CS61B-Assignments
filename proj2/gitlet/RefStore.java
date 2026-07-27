package gitlet;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitlet.Utils.*;
import static gitlet.Paths.*;

public class RefStore {

    /** Create a branch with given name and commit id */
    public static void createBranch(String branchName, String id) {
        File file = join(REFS_DIR, branchName);
        if (fileExists(file)) {
            message("A branch with that name already exists.");
            System.exit(0);
        } else {
            writeContents(file, id);
        }
    }

    /** Create a branch with given name */
    public static void createBranch(String branch) {
        createBranch(branch, getHeadCommitId());
    }

    /** Remove the branch */
    public static void removeBranch(String branch) {
        File file = join(REFS_DIR, branch);
        if (!fileExists(file)) {
            message("A branch with that name does not exist.");
        } else if (branch.equals(getHeadName())) {
            message("Cannot remove the current branch.");
        } else if (file.delete()) {
            return;
        }
        System.exit(0);
    }

    /** Set the branch in HEAD */
    public static void setHead(String branch) {
        writeContents(HEAD_FILE, REFS_DIR.getName() + "/" + branch);
    }

    /** Set the commit id that HEAD point to */
    public static void setHeadCommitId(String id) {
        String branch = readContentsAsString(HEAD_FILE);
        writeContents(join(GITLET_DIR, branch), id);
    }

    /** Get the commit id that HEAD point to */
    public static String getHeadCommitId() {
        String branch = readContentsAsString(HEAD_FILE);
        return readContentsAsString(join(GITLET_DIR, branch));
    }

    /** Get the commit object that HEAD point to */
    public static Commit getHeadCommit() {
        return Persistence.loadCommit(getHeadCommitId());
    }

    /** Get the branch name of current HEAD */
    public static String getHeadName() {
        Pattern pattern = Pattern.compile("refs/(?<branchName>\\w+)");
        Matcher matcher = pattern.matcher(readContentsAsString(HEAD_FILE));
        if (matcher.find()) {
            return matcher.group("branchName");
        } else {
            message("Fail to get Head's branch name");
            System.exit(0);
            return "";
        }
    }

    /** Get the commit object that the branch point to */
    public static Commit getBranchCommit(String branch) {
        return Persistence.loadCommit(getBranchCommitId(branch));
    }

    /** Get the branch's commit id */
    public static String getBranchCommitId(String branch) {
        return readContentsAsString(join(REFS_DIR, branch));
    }

    /** Check the given branch exists or not */
    public static boolean branchExists(String branch) {
        return fileExists(join(REFS_DIR, branch));
    }

    /** List all branch names */
    public static List<String> allBranchNames() {
        return plainFilenamesIn(REFS_DIR);
    }

}
