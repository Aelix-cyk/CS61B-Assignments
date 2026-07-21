package gitlet.service;

import gitlet.core.Commit;
import gitlet.core.Index;
import gitlet.storage.Persistence;
import gitlet.storage.RefStore;

import java.util.*;

import static gitlet.Utils.*;
import static gitlet.util.Paths.*;

/** Handles all checkout operations: file-level, commit-level, and branch-level.
 *  Shared by the checkout and reset commands.
 *  @author Aelix
 */
public class CheckoutService {

    private final Persistence persistence;
    private final RefStore refStore;

    public CheckoutService(Persistence persistence, RefStore refStore) {
        this.persistence = persistence;
        this.refStore = refStore;
    }

    /** Checkout a file from the HEAD commit. */
    public void checkoutFileFromHead(String filename) {
        String headId = refStore.getBranchCommitId(refStore.getCurrentBranchName());
        Commit head = persistence.loadCommit(headId);
        if (!head.hasFile(filename)) {
            message("File does not exist in that commit.");
            System.exit(0);
        }
        restoreFile(filename, head.trackedFileId(filename));
    }

    /** Checkout a file from a specific commit (by short or full id). */
    public void checkoutFileFromCommit(String shortId, String filename) {
        String id = persistence.resolveShortId(shortId);
        Commit commit = persistence.loadCommit(id);
        if (commit.hasFile(filename)) {
            restoreFile(filename, commit.trackedFileId(filename));
        } else {
            message("File does not exist in that commit.");
        }
    }

    /** Switch to the given branch. */
    public void checkoutBranch(String branchName) {
        if (!refStore.branchExists(branchName)) {
            message("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(refStore.getCurrentBranchName())) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }
        checkoutCommit(refStore.getBranchCommitId(branchName));
        refStore.setHeadRef(branchName);
    }

    /** Checkout an arbitrary commit by id.
     *  Handles file deletion, creation, overwrite, and untracked-file safety.
     */
    public void checkoutCommit(String commitId) {
        List<String> filesInCWD = plainFilenamesIn(CWD);
        if (filesInCWD == null) {
            filesInCWD = new ArrayList<>();
        }

        String headId = refStore.getBranchCommitId(refStore.getCurrentBranchName());
        Commit currentCommit = persistence.loadCommit(headId);
        Commit targetCommit = persistence.loadCommit(commitId);
        Map<String, String> targetFiles = targetCommit.getTrackedFiles();

        Set<String> filesToDelete = new HashSet<>();
        Set<String> filesToCreate = new HashSet<>();
        Set<String> filesToOverwrite = new HashSet<>();

        /* Files tracked by current commit, not in target commit → delete */
        for (String file : filesInCWD) {
            if (currentCommit.hasFile(file)) {
                String fileId = sha1((Object) readContents(join(CWD, file)));
                if (currentCommit.hasSameFile(file, fileId) && !targetCommit.hasFile(file)) {
                    filesToDelete.add(file);
                }
            }
        }

        /* Files in target commit: create new or overwrite existing */
        for (String file : targetFiles.keySet()) {
            if (filesInCWD.contains(file)) {
                String fileId = sha1((Object) readContents(join(CWD, file)));
                if (!targetCommit.hasSameFile(file, fileId)
                        && !currentCommit.hasSameFile(file, fileId)) {
                    printUntrackedFileError();
                }
                filesToOverwrite.add(file);
            } else {
                filesToCreate.add(file);
            }
        }

        /* Apply deletions */
        for (String file : filesToDelete) {
            restrictedDelete(join(CWD, file));
        }
        /* Apply creations */
        for (String file : filesToCreate) {
            restoreFile(file, targetFiles.get(file));
        }
        /* Apply overwrites */
        for (String file : filesToOverwrite) {
            String currentId = sha1((Object) readContents(join(CWD, file)));
            String targetId = targetFiles.get(file);
            if (!currentId.equals(targetId)) {
                restoreFile(file, targetId);
            }
        }

        /* Clear the staging area */
        Persistence.saveIndex(new Index());
    }

    /** Write a blob to the working directory. */
    public void restoreFile(String filename, String blobId) {
        writeContents(join(CWD, filename), (Object) persistence.loadBlob(blobId));
    }

    /** Print error and exit when untracked files would be overwritten. */
    public static void printUntrackedFileError() {
        message("There is an untracked file in the way; delete it, or add and commit it first.");
        System.exit(0);
    }
}
