package gitlet;

import java.util.*;

import static gitlet.Paths.CWD;
import static gitlet.Persistence.restoreFile;
import static gitlet.RefStore.*;
import static gitlet.Utils.*;

public class CheckoutService {

    /** Check out the given file in previous commit */
    public static void checkoutFileFromHead(String file) {
        Commit commit = getHeadCommit();
        if (!commit.hasFile(file)) {
            message("File does not exist in that commit.");
            System.exit(0);
        }
        restoreFile(file, commit.trackedFileId(file));
    }

    /** Check out the given file in given commit */
    public static void checkoutFileFromCommit(String shortId, String file) {
        String id = Persistence.getLongId(shortId);
        Commit commit = Persistence.loadCommit(id);
        if (commit.hasFile(file)) {
            restoreFile(file, commit.trackedFileId(file));
        } else {
            message("File does not exist in that commit.");
        }
    }

    /** Check out the given branch */
    public static void checkoutBranch(String branch) {
        if (!RefStore.branchExists(branch)) {
            message( "No such branch exists.");
            System.exit(0);
        }
        /* check that branch is not current branch */
        String currentBranch = getHeadName();
        if (branch.equals(currentBranch)) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }

        checkoutCommit(getBranchCommitId(branch));
        setHead(branch);
    }

    /** Checkout the given commit */
    public static void checkoutCommit(String id) {
        /* check untracked files that would be overwritten */
        List<String> files = plainFilenamesIn(CWD);
        if (files == null) {
            files = new ArrayList<>();
        }
        Commit currentCommit = getHeadCommit();
        Commit checkOutCommit = Persistence.loadCommit(id);
        Map<String, String> checkOutTrackedFiles = checkOutCommit.getTrackedFiles();
        HashSet<String> filesToOverwrite = new HashSet<>();
        HashSet<String> filesToDelete = new HashSet<>();
        HashSet<String> filesToCreate = new HashSet<>();

        for (String file : files) {
            if (currentCommit.hasFile(file)) {
                String fileId = sha1((Object) readContents(join(CWD, file)));
                if (currentCommit.hasSameFile(file, fileId) && !checkOutCommit.hasFile(file)) {
                    filesToDelete.add(file);
                }
            }
        }

        for (String file : checkOutTrackedFiles.keySet()) {
            if (files.contains(file)) {
                String fileId = sha1((Object) readContents(join(CWD, file)));
                if (!checkOutCommit.hasSameFile(file, fileId) && !currentCommit.hasSameFile(file, fileId)) {
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
                filesToOverwrite.add(file);
            } else {
                filesToCreate.add(file);
            }
        }

        for (String file : filesToDelete) {
            restrictedDelete(join(CWD, file));
        }
        for (String file : filesToCreate) {
            restoreFile(file, checkOutTrackedFiles.get(file));
        }
        for (String file : filesToOverwrite) {
            String currentId = sha1((Object) readContents(join(CWD, file)));
            String checkOutId = checkOutTrackedFiles.get(file);
            if (!currentId.equals(checkOutId)) {
                restoreFile(file, checkOutId);
            }
        }
        Persistence.saveIndex(new Index());
    }

}
