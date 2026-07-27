package gitlet;

import java.util.*;

import static gitlet.Utils.*;

public class StatusService {

    /** Return the status message */
    public static String getStatus() {
        StringBuilder output = new StringBuilder();

        /* status of branches */
        ArrayList<String> branchNames = new ArrayList<>(RefStore.allBranchNames());
        String currentBranch = RefStore.getHeadName();

        output.append("=== Branches ===\n");
        branchNames.remove(currentBranch);
        branchNames.add("*" + currentBranch);
        output.append(sortedStatusString(branchNames));

        /* status of staged files and removed files */
        Index index = Persistence.loadIndex();
        Map<String, String> additionMap = index.getAdditionMap();
        Set<String> removalSet = index.getRemovalSet();

        output.append("\n=== Staged Files ===\n");
        output.append(sortedStatusString(additionMap.keySet()));
        output.append("\n=== Removed Files ===\n");
        output.append(sortedStatusString(removalSet));

        /* status of modifications that are not staged for commit */
        List<String> filesInCWD = plainFilenamesIn(Paths.CWD);
        Commit currentCommit = RefStore.getHeadCommit();
        Map<String, String> commitMap = currentCommit.getTrackedFiles();
        HashSet<String> modifiedFiles = new HashSet<>();
        HashSet<String> untrackedFiles = new HashSet<>();
        if (filesInCWD != null) {
            /* traverse files in current directory */
            for (String file : filesInCWD) {
                String id = sha1((Object) readContents(join(Paths.CWD, file)));
                /* not in current commit and not in additionMap */
                if (!currentCommit.hasFile(file) && !index.hasFile(file)) {
                    untrackedFiles.add(file);
                }

                if (!index.hasSameFile(file, id)) {
                    /* staged for addition but changed */
                    if (index.hasFile(file)) {
                        modifiedFiles.add(file + " (modified)");
                    }
                    /* tracked in commit but changed and not staged */
                    if (currentCommit.hasFile(file) && !currentCommit.hasSameFile(file, id)) {
                        modifiedFiles.add(file + " (modified)");
                    }
                }
            }

            /* find deleted files */
            for (String file : commitMap.keySet()) {
                if (!filesInCWD.contains(file) && !removalSet.contains(file)) {
                    modifiedFiles.add(file + " (deleted)");
                }
            }
            for (String file : additionMap.keySet()) {
                if (!filesInCWD.contains(file)) {
                    modifiedFiles.add(file + " (deleted)");
                }
            }
        }

        output.append("\n=== Modifications Not Staged For Commit ===\n");
        output.append(sortedStatusString(modifiedFiles));
        output.append("\n=== Untracked Files ===\n");
        output.append(sortedStatusString(untrackedFiles)).append("\n");

        return output.toString();
    }

    private static String sortedStatusString(Collection<String> collection) {
        String[] files = collection.toArray(new String[0]);
        Arrays.sort(files);
        StringBuilder sorted = new StringBuilder();
        for (String file : files) {
            sorted.append(file).append("\n");
        }
        return sorted.toString();
    }

}
