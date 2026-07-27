package gitlet;

import java.util.*;

import static gitlet.Paths.CWD;
import static gitlet.Utils.*;

public class MergeService {

    public static class MergePlan {
        // files to be checked out, removed and conflict
        Set<String> checkoutSet;
        Set<String> removeSet;
        Set<String> conflictSet;

        MergePlan() {
            checkoutSet = new HashSet<>();
            removeSet = new HashSet<>();
            conflictSet = new HashSet<>();
        }
    }

    /** Classify files and return the MergePlan */
    public static MergePlan classifyFiles(Commit splitCommit, Commit currentCommit, Commit givenCommit) {
        MergePlan mergePlan = new MergePlan();
        /* check untracked files that would be overwritten */
        List<String> filesInCWD = plainFilenamesIn(CWD);
        if (filesInCWD == null) {
            filesInCWD = new ArrayList<String>();
        }
        HashMap<String, String> splitMap = new HashMap<>(splitCommit.getTrackedFiles());
        HashMap<String, String> currentMap = new HashMap<>(currentCommit.getTrackedFiles());
        HashMap<String, String> givenMap = new HashMap<>(givenCommit.getTrackedFiles());
        // files to be checked out, removed and conflict
        HashSet<String> currentRemainSet = new HashSet<>(currentMap.keySet());
        HashSet<String> givenRemainSet = new HashSet<>(givenMap.keySet());

        for (String file : splitMap.keySet()) {
            String currentId = currentMap.get(file);
            String splitId = splitMap.get(file);
            String givenId = givenMap.get(file);

            currentRemainSet.remove(file);
            givenRemainSet.remove(file);
            if (currentMap.containsKey(file) && currentId.equals(splitId)) {
                /* current file is clean */
                /* file is modified or removed in given branch */
                if (givenMap.containsKey(file)) {
                    if (!currentId.equals(givenId)) {
                        mergePlan.checkoutSet.add(file);
                    }
                } else {
                    mergePlan.removeSet.add(file);
                }
            } else if (currentMap.containsKey(file) && !currentId.equals((splitId))) {
                /* current file is modified */
                if (!givenMap.containsKey(file)) {
                    /* given file is removed */
                    mergePlan.conflictSet.add(file);
                } else if (!givenId.equals(currentId) && !givenId.equals(splitId)) {
                    /* given file is modified in different way */
                    mergePlan.conflictSet.add(file);
                }

            } else {
                /* current file is deleted and given file is modified in different way */
                if (givenMap.containsKey(file) && !givenId.equals(splitId)) {
                    mergePlan.conflictSet.add(file);
                }
            }
        }

        /* files in given branch, but not in split point */
        for (String file : givenRemainSet) {
            if (!currentRemainSet.contains(file)) {
                mergePlan.checkoutSet.add(file);
            } else if (!givenMap.get(file).equals(currentMap.get(file))) {
                mergePlan.conflictSet.add(file);
            }
        }

        /* check untracked files */
        for (String file : filesInCWD) {
            if (mergePlan.checkoutSet.contains(file)
                    || mergePlan.removeSet.contains(file)
                    || mergePlan.conflictSet.contains(file)) {
                String fileId = sha1((Object) readContents(join(CWD, file)));
                if (!fileId.equals(currentMap.get(file))) {
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        return mergePlan;
    }

    /** Find the split point between two branch */
    public static String findSplitPoint(String branchA, String branchB) {
        String[] branches = new String[]{branchA, branchB};
        List<HashSet<String>> commitsSets = new ArrayList<>();
        List<ArrayDeque<String>> commitsDeques = new ArrayList<>();
        /* initialize */
        for (int i = 0; i < branches.length; i += 1) {
            commitsSets.add(new HashSet<>());
            commitsDeques.add(new ArrayDeque<>());
        }
        for (int i = 0; i < branches.length; i += 1) {
            commitsDeques.get(i).addLast(RefStore.getBranchCommitId(branches[i]));
            commitsSets.get(i).add(commitsDeques.get(i).getFirst());
        }

        /* use BFS to find split point */
        while (true) {
            for (int i = 0; i < branches.length; i += 1) {
                ArrayDeque<String> tempDeque = new ArrayDeque<>(commitsDeques.get(i));
                commitsDeques.get(i).clear();
                for (String id : tempDeque) {
                    if (commitsSets.get(1 - i).contains(id)) {
                        return id;
                    }
                    Commit commit = Persistence.loadCommit(id);
                    if (commit.getParent() != null) {
                        commitsDeques.get(i).addLast(commit.getParent());
                        commitsSets.get(i).add(commit.getParent());
                    }
                    if (commit.getSecondParent() != null) {
                        commitsDeques.get(i).addLast(commit.getSecondParent());
                        commitsSets.get(i).add(commit.getSecondParent());
                    }
                }
            }
        }
    }

    /** Write the conflict file with special format */
    public static void writeConflictFile(String fileName, Commit current, Commit given) {
        byte[] currentContents, givenContents;
        if (current.hasFile(fileName)) {
            currentContents = Persistence.loadBlob(current.trackedFileId(fileName)).getContents();
        } else {
            currentContents = new byte[]{};
        }
        if (given.hasFile(fileName)) {
            givenContents = Persistence.loadBlob(given.trackedFileId(fileName)).getContents();
        } else {
            givenContents = new byte[]{};
        }

        writeContents(join(CWD, fileName), "<<<<<<< HEAD\n", currentContents, "=======\n", givenContents, ">>>>>>>\n");
    }

}
