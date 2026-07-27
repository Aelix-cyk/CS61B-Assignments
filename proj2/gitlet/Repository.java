package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Paths.CWD;
import static gitlet.Persistence.restoreFile;
import static gitlet.RefStore.*;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  It contains the main logic of gitlet.
 *  @author Aelix
 */
public class Repository {


    /** Command: init */
    public static void init() {
        if (Paths.GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        Persistence.init();

        String defaultBranch = "master";
        Commit initialCommit = new Commit("initial commit");
        initialCommit.setEpochTime();
        String id = Persistence.saveCommit(initialCommit);
        createBranch(defaultBranch, id);
        setHead(defaultBranch);
    }

    /** Command: add */
    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            message("File does not exist.");
            System.exit(0);
        }

        Blob blob = new Blob(readContents(file));
        Commit commit = getHeadCommit();
        Index index = Persistence.loadIndex();

        if (!commit.hasSameFile(fileName, blob.getId())) {
           if (!index.hasSameFile(fileName, blob.getId())) {
               index.stageForAddition(fileName, blob.getId());
               Persistence.saveBlob(blob);
           }
        } else {
            index.unstage(fileName);
        }
        Persistence.saveIndex(index);
    }

    /** Create a new commit and move HEAD pointer to it */
    public static void commit(String commitMessage) {
        commitWithParent(commitMessage, null, Persistence.loadIndex());
    }

    public static void commitWithParent(String message, String secondParentId, Index index) {
        if (index.isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        }

        Commit parentCommit = getHeadCommit();
        Commit newCommit = new Commit(message, parentCommit, getHeadCommitId());
        if (secondParentId != null) {
            newCommit.setSecondParent(secondParentId);
        }

        /* process files in staging area */
        newCommit.addFromMap(index.getAdditionMap());
        newCommit.removeFromSet(index.getRemovalSet());
        index.clear();
        Persistence.saveIndex(index);

        /* save commit and update HEAD pointer */
        String id = Persistence.saveCommit(newCommit);
        setHeadCommitId(id);
    }

    /** Remove file from staging area, add it to stage for removal */
    public static void rm(String fileName) {
        File file = join(CWD, fileName);
        Index index = Persistence.loadIndex();
        Commit commit = getHeadCommit();

        if (!index.hasFile(fileName) && !commit.hasFile(fileName)) {
            message("No reason to remove the file.");
            System.exit(0);
        }

        if (index.hasFile(fileName)) {
            index.unstage(fileName);
        }

        if (commit.hasFile(fileName)) {
            index.stageForRemoval(fileName);
            if (file.exists()) {
                restrictedDelete(file);
            }
        }
        Persistence.saveIndex(index);
    }

    /** Print commit log */
    public static void log() {
        Commit commit = getHeadCommit();
        String id = getHeadCommitId();
        StringBuilder log = new StringBuilder();

        while (true) {
            log.append(commit.dumpLog(id));
            id = commit.getParent();
            if (id != null) {
                commit = Persistence.loadCommit(id);
            } else {
                break;
            }
        }
        System.out.print(log);
    }

    /** Print global commit log */
    public static void globalLog() {
        List<String> fileList = plainFilenamesIn(Paths.COMMITS_DIR);
        StringBuilder log = new StringBuilder();

        if (fileList != null) {
            for (String id : fileList) {
                Commit commit = Persistence.loadCommit(id);
                log.append(commit.dumpLog(id));
            }
            System.out.print(log);
        }
    }

    /** Find commits with given commit message */
    public static void find(String message) {
        List<String> fileList = plainFilenamesIn(Paths.COMMITS_DIR);
        StringBuilder log = new StringBuilder();

        if (fileList != null) {
            for (String id : fileList) {
                String commitMessage = Persistence.loadCommit(id).getMessage();

                if (commitMessage.contains(message)) {
                    log.append(id);
                    log.append("\n");
                }
            }

            if (log.length() > 0) {
                System.out.print(log);
            } else {
                message("Found no commit with that message.");
            }
        }
    }

    /** Show status of gitlet system */
    public static void status() {
        System.out.print(StatusService.getStatus());
    }

    /** Checkout file or commit */
    public static void checkout(String[] args) {
        if (args.length == 2) {
            CheckoutService.checkoutBranch(args[1]);
        } else if (args.length == 3 && args[1].equals("--")) {
            CheckoutService.checkoutFileFromHead(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            CheckoutService.checkoutFileFromCommit(args[1], args[3]);
        } else {
            Utils.message("Incorrect operands.");
        }
    }

    /** Create branch */
    public static void branch(String branchName) {
        RefStore.createBranch(branchName);
    }

    /** Remove branch */
    public static void rmBranch(String branchName) {
        removeBranch(branchName);
    }

    /** Reset with the given commit id */
    public static void reset(String shortId) {
        String id = Persistence.getLongId(shortId);
        CheckoutService.checkoutCommit(id);
        setHeadCommitId(id);
    }

    /** Merge the files in given branch to current branch */
    public static void merge(String branch) {
        Index index = Persistence.loadIndex();
        /* check error cases */
        if (!index.isEmpty()) {
            message("You have uncommitted changes.");
            System.exit(0);
        }
        if (!branchExists(branch)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch.equals(getHeadName())) {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        String splitPointId = MergeService.findSplitPoint(getHeadName(), branch);
        String givenCommitId = getBranchCommitId(branch);
        /* check special cases */
        if (splitPointId.equals(givenCommitId)) {
            message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPointId.equals(getHeadCommitId())) {
            message("Current branch fast-forwarded.");
        }

        Commit splitCommit = Persistence.loadCommit(splitPointId);
        Commit currentCommit = getHeadCommit();
        Commit givenCommit = Persistence.loadCommit(givenCommitId);
        MergeService.MergePlan mergePlan = MergeService.classifyFiles(splitCommit, currentCommit, givenCommit);

        /* check out files in checkOutSet */
        for (String file : mergePlan.checkoutSet) {
            restoreFile(file, givenCommit.trackedFileId(file));
            Blob blob = new Blob(readContents(join(CWD, file)));
            index.stageForAddition(file, blob.getId());
            Persistence.saveBlob(blob);
        }
        /* remove files in removeSet */
        for (String file : mergePlan.removeSet) {
            index.stageForRemoval(file);
            restrictedDelete(join(CWD, file));
        }
        /* update files in conflictSet */
        for (String file : mergePlan.conflictSet) {
            MergeService.writeConflictFile(file, currentCommit, givenCommit);
            Blob blob = new Blob(readContents(join(CWD, file)));
            index.stageForAddition(file, blob.getId());
            Persistence.saveBlob(blob);
        }

        commitWithParent("Merged " + branch + " into " + getHeadName() + ".", getBranchCommitId(branch), index);
        if (!mergePlan.conflictSet.isEmpty()) {
            message("Encountered a merge conflict.");
        }
    }

}
