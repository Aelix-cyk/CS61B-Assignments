package gitlet.service;

import gitlet.core.Commit;
import gitlet.storage.Persistence;
import gitlet.storage.RefStore;

import java.util.*;

import static gitlet.Utils.*;
import static gitlet.util.Paths.*;

/** Handles merge operations: split-point discovery, 3-way file classification,
 *  conflict file generation, and untracked file safety checks.
 *  @author Aelix
 */
public class MergeService {

    private final Persistence persistence;
    private final RefStore refStore;

    public MergeService(Persistence persistence, RefStore refStore) {
        this.persistence = persistence;
        this.refStore = refStore;
    }

    /** Result of classifying files during a 3-way merge. */
    public static class MergePlan {
        public final Set<String> filesToCheckout;
        public final Set<String> filesToRemove;
        public final Set<String> filesToConflict;

        public MergePlan(Set<String> filesToCheckout,
                         Set<String> filesToRemove,
                         Set<String> filesToConflict) {
            this.filesToCheckout = filesToCheckout;
            this.filesToRemove = filesToRemove;
            this.filesToConflict = filesToConflict;
        }
    }

    /** Find the split point (latest common ancestor) of two branches using BFS. */
    public String findSplitPoint(String branchA, String branchB) {
        String[] branches = new String[]{branchA, branchB};
        List<Set<String>> commitSets = new ArrayList<>();
        List<ArrayDeque<String>> commitDeques = new ArrayList<>();

        for (int i = 0; i < branches.length; i++) {
            commitSets.add(new HashSet<>());
            commitDeques.add(new ArrayDeque<>());
        }
        for (int i = 0; i < branches.length; i++) {
            commitDeques.get(i).addLast(refStore.getBranchCommitId(branches[i]));
            commitSets.get(i).add(commitDeques.get(i).getFirst());
        }

        while (true) {
            for (int i = 0; i < branches.length; i++) {
                ArrayDeque<String> tempDeque = new ArrayDeque<>(commitDeques.get(i));
                commitDeques.get(i).clear();
                for (String id : tempDeque) {
                    if (commitSets.get(1 - i).contains(id)) {
                        return id;
                    }
                    Commit commit = persistence.loadCommit(id);
                    if (commit.getParent() != null) {
                        commitDeques.get(i).addLast(commit.getParent());
                        commitSets.get(i).add(commit.getParent());
                    }
                    if (commit.getSecondParent() != null) {
                        commitDeques.get(i).addLast(commit.getSecondParent());
                        commitSets.get(i).add(commit.getSecondParent());
                    }
                }
            }
        }
    }

    /** Classify every file across split, current, and given commits into a MergePlan. */
    public MergePlan classifyFiles(Commit splitCommit,
                                   Commit currentCommit,
                                   Commit givenCommit) {
        Map<String, String> splitMap = new HashMap<>(splitCommit.getTrackedFiles());
        Map<String, String> currentMap = new HashMap<>(currentCommit.getTrackedFiles());
        Map<String, String> givenMap = new HashMap<>(givenCommit.getTrackedFiles());

        Set<String> checkOutSet = new HashSet<>();
        Set<String> removeSet = new HashSet<>();
        Set<String> conflictSet = new HashSet<>();
        Set<String> currentRemain = new HashSet<>(currentMap.keySet());
        Set<String> givenRemain = new HashSet<>(givenMap.keySet());

        for (String file : splitMap.keySet()) {
            String currentId = currentMap.get(file);
            String splitId = splitMap.get(file);
            String givenId = givenMap.get(file);

            currentRemain.remove(file);
            givenRemain.remove(file);

            if (currentMap.containsKey(file) && currentId.equals(splitId)) {
                /* File unchanged in current branch */
                if (givenMap.containsKey(file)) {
                    if (!currentId.equals(givenId)) {
                        checkOutSet.add(file);
                    }
                } else {
                    removeSet.add(file);
                }
            } else if (currentMap.containsKey(file) && !currentId.equals(splitId)) {
                /* File modified in current branch */
                if (!givenMap.containsKey(file)) {
                    conflictSet.add(file);
                } else if (!givenId.equals(currentId) && !givenId.equals(splitId)) {
                    conflictSet.add(file);
                }
            } else {
                /* File deleted in current branch */
                if (givenMap.containsKey(file) && !givenId.equals(splitId)) {
                    conflictSet.add(file);
                }
            }
        }

        /* Files in given branch but not in split point */
        for (String file : givenRemain) {
            if (!currentRemain.contains(file)) {
                checkOutSet.add(file);
            } else if (!givenMap.get(file).equals(currentMap.get(file))) {
                conflictSet.add(file);
            }
        }

        return new MergePlan(checkOutSet, removeSet, conflictSet);
    }

    /** Write a conflict file with standard markers. */
    public void writeConflictFile(String filename, Commit currentCommit, Commit givenCommit) {
        byte[] currentContents;
        byte[] givenContents;

        if (currentCommit.hasFile(filename)) {
            currentContents = persistence.loadBlob(currentCommit.trackedFileId(filename));
        } else {
            currentContents = new byte[]{};
        }
        if (givenCommit.hasFile(filename)) {
            givenContents = persistence.loadBlob(givenCommit.trackedFileId(filename));
        } else {
            givenContents = new byte[]{};
        }

        writeContents(join(CWD, filename),
                "<<<<<<< HEAD\n", currentContents,
                "=======\n", givenContents,
                ">>>>>>>\n");
    }

    /** Check that no untracked files would be overwritten by the merge plan. */
    public void checkUntrackedOverwrite(MergePlan plan, Commit currentCommit) {
        List<String> filesInCWD = plainFilenamesIn(CWD);
        if (filesInCWD == null) {
            return;
        }
        for (String file : filesInCWD) {
            if (plan.filesToCheckout.contains(file)
                    || plan.filesToRemove.contains(file)
                    || plan.filesToConflict.contains(file)) {
                String fileId = sha1((Object) readContents(join(CWD, file)));
                if (!currentCommit.hasSameFile(file, fileId)) {
                    CheckoutService.printUntrackedFileError();
                }
            }
        }
    }
}
