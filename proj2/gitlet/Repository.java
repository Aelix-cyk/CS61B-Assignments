package gitlet;

import gitlet.core.Blob;
import gitlet.core.Commit;
import gitlet.core.Index;
import gitlet.service.CheckoutService;
import gitlet.service.MergeService;
import gitlet.service.MergeService.MergePlan;
import gitlet.service.StatusService;
import gitlet.storage.Persistence;
import gitlet.storage.RefStore;

import java.util.List;

import static gitlet.Utils.*;
import static gitlet.util.Paths.*;

/** Gitlet repository facade.
 *  Holds all components and exposes command-level instance methods.
 *  @author Aelix
 */
public class Repository {

    private final Persistence persistence;
    private final RefStore refStore;
    private final CheckoutService checkoutService;
    private final MergeService mergeService;
    private final StatusService statusService;

    public Repository() {
        persistence = new Persistence();
        refStore = new RefStore();
        checkoutService = new CheckoutService(persistence, refStore);
        mergeService = new MergeService(persistence, refStore);
        statusService = new StatusService(persistence, refStore);
    }

    /* ==================== Convenience Delegators ==================== */

    public String getHeadCommitId() {
        return refStore.getBranchCommitId(refStore.getCurrentBranchName());
    }

    public Commit getHeadCommit() {
        return persistence.loadCommit(getHeadCommitId());
    }

    public String getCurrentBranchName() {
        return refStore.getCurrentBranchName();
    }

    public Index getIndex() {
        return persistence.loadIndex();
    }

    public void saveIndex(Index index) {
        Persistence.saveIndex(index);
    }

    /* ==================== init ==================== */

    public void init() {
        if (!Persistence.repoExists()) {
            String defaultBranch = "master";
            Commit initialCommit = new Commit("initial commit");
            Persistence.init();
            initialCommit.setEpochTime();
            String id = persistence.saveCommit(initialCommit);
            refStore.createBranch(defaultBranch, id);
            refStore.setHeadRef(defaultBranch);
        } else {
            message("A Gitlet version-control system already exists in the current directory.");
        }
    }

    /* ==================== add ==================== */

    public void add(String filename) {
        Index index = getIndex();
        Blob blob = index.addFile(join(CWD, filename), getHeadCommit());
        if (blob != null) {
            persistence.saveBlob(blob);
        }
        saveIndex(index);
    }

    /* ==================== commit ==================== */

    public void commit(String message) {
        commitWithParent(message, null);
    }

    public void commitWithParent(String message, String secondParentId) {
        Index index = getIndex();
        if (index.isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        }

        Commit parentCommit = getHeadCommit();
        Commit newCommit = new Commit(message, parentCommit, getHeadCommitId());
        if (secondParentId != null) {
            newCommit.setSecondParent(secondParentId);
        }

        newCommit.addFromMap(index.getAdditionMap());
        newCommit.removeFromSet(index.getRemovalSet());
        index.clear();
        saveIndex(index);

        String id = persistence.saveCommit(newCommit);
        refStore.setBranchCommitId(getCurrentBranchName(), id);
    }

    /* ==================== rm ==================== */

    public void rm(String filename) {
        Index index = getIndex();
        index.removeFile(join(CWD, filename), getHeadCommit());
        saveIndex(index);
    }

    /* ==================== log ==================== */

    public void log() {
        Commit commit = getHeadCommit();
        String id = getHeadCommitId();
        StringBuilder logStr = new StringBuilder();

        while (true) {
            logStr.append(commit.dumpLog(id));
            id = commit.getParent();
            if (id != null) {
                commit = persistence.loadCommit(id);
            } else {
                break;
            }
        }
        System.out.print(logStr);
    }

    /* ==================== global-log ==================== */

    public void globalLog() {
        List<String> ids = persistence.allCommitIds();
        if (ids != null) {
            for (String id : ids) {
                Commit commit = persistence.loadCommit(id);
                System.out.print(commit.dumpLog(id));
            }
        }
    }

    /* ==================== find ==================== */

    public void find(String message) {
        List<String> ids = persistence.allCommitIds();
        StringBuilder result = new StringBuilder();

        if (ids != null) {
            for (String id : ids) {
                String commitMessage = persistence.loadCommit(id).getMessage();
                if (commitMessage.contains(message)) {
                    result.append(id).append("\n");
                }
            }
            if (result.length() > 0) {
                System.out.print(result);
            } else {
                message("Found no commit with that message.");
            }
        }
    }

    /* ==================== status ==================== */

    public void status() {
        System.out.print(statusService.buildStatusString());
    }

    /* ==================== checkout ==================== */

    public void checkoutFile(String filename) {
        checkoutService.checkoutFileFromHead(filename);
    }

    public void checkoutCommitFile(String commitId, String filename) {
        checkoutService.checkoutFileFromCommit(commitId, filename);
    }

    public void checkoutBranch(String branchName) {
        if (!refStore.branchExists(branchName)) {
            message("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(getCurrentBranchName())) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }
        checkoutService.checkoutBranch(branchName);
    }

    /* ==================== branch ==================== */

    public void createBranch(String branchName) {
        refStore.createBranch(branchName, getHeadCommitId());
    }

    /* ==================== rm-branch ==================== */

    public void removeBranch(String branchName) {
        refStore.deleteBranch(branchName);
    }

    /* ==================== reset ==================== */

    public void reset(String shortId) {
        String id = persistence.resolveShortId(shortId);
        checkoutService.checkoutCommit(id);
        refStore.setBranchCommitId(getCurrentBranchName(), id);
    }

    /* ==================== merge ==================== */

    public void merge(String branchName) {
        if (!getIndex().isEmpty()) {
            message("You have uncommitted changes.");
            System.exit(0);
        }
        if (!refStore.branchExists(branchName)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(getCurrentBranchName())) {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        String splitPointId = mergeService.findSplitPoint(getCurrentBranchName(), branchName);

        if (splitPointId.equals(refStore.getBranchCommitId(branchName))) {
            message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPointId.equals(getHeadCommitId())) {
            message("Current branch fast-forwarded.");
        }

        Commit splitCommit = persistence.loadCommit(splitPointId);
        Commit currentCommit = getHeadCommit();
        Commit givenCommit = persistence.loadCommit(refStore.getBranchCommitId(branchName));

        MergePlan plan = mergeService.classifyFiles(splitCommit, currentCommit, givenCommit);
        mergeService.checkUntrackedOverwrite(plan, currentCommit);

        Index index = getIndex();
        for (String file : plan.filesToCheckout) {
            checkoutService.restoreFile(file, givenCommit.trackedFileId(file));
            Blob blob = index.addFile(join(CWD, file), currentCommit);
            if (blob != null) {
                persistence.saveBlob(blob);
            }
        }
        for (String file : plan.filesToRemove) {
            index.removeFile(join(CWD, file), currentCommit);
        }
        for (String file : plan.filesToConflict) {
            mergeService.writeConflictFile(file, currentCommit, givenCommit);
            Blob blob = index.addFile(join(CWD, file), currentCommit);
            if (blob != null) {
                persistence.saveBlob(blob);
            }
        }
        saveIndex(index);

        String mergeMessage = "Merged " + branchName + " into " + getCurrentBranchName() + ".";
        commitWithParent(mergeMessage, refStore.getBranchCommitId(branchName));

        if (!plan.filesToConflict.isEmpty()) {
            message("Encountered a merge conflict.");
        }
    }
}
