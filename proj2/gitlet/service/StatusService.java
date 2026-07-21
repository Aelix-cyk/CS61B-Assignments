package gitlet.service;

import gitlet.core.Commit;
import gitlet.core.Index;
import gitlet.storage.Persistence;
import gitlet.storage.RefStore;

import java.util.*;

import static gitlet.Utils.*;
import static gitlet.util.Paths.*;

/** Gathers and formats the status report for the Gitlet repository.
 *  @author Aelix
 */
public class StatusService {

    private final Persistence persistence;
    private final RefStore refStore;

    public StatusService(Persistence persistence, RefStore refStore) {
        this.persistence = persistence;
        this.refStore = refStore;
    }

    /** Builds the full status output string:
     *  === Branches ===
     *  === Staged Files ===
     *  === Removed Files ===
     *  === Modifications Not Staged For Commit ===
     *  === Untracked Files ===
     */
    public String buildStatusString() {
        StringBuilder output = new StringBuilder();

        /* Branches */
        List<String> branchNames = new ArrayList<>(refStore.allBranchNames());
        String currentBranch = refStore.getCurrentBranchName();
        output.append("=== Branches ===\n");
        branchNames.remove(currentBranch);
        branchNames.add("*" + currentBranch);
        output.append(sortedString(branchNames));

        /* Staged and removed files */
        Index index = persistence.loadIndex();
        output.append("\n=== Staged Files ===\n");
        output.append(sortedString(index.getAdditionMap().keySet()));
        output.append("\n=== Removed Files ===\n");
        output.append(sortedString(index.getRemovalSet()));

        /* Modified and untracked files */
        List<String> filesInCWD = plainFilenamesIn(CWD);
        if (filesInCWD == null) {
            filesInCWD = new ArrayList<>();
        }

        String headId = refStore.getBranchCommitId(currentBranch);
        Commit headCommit = persistence.loadCommit(headId);
        Set<String> modifiedFiles = new HashSet<>();
        Set<String> untrackedFiles = new HashSet<>();

        for (String file : filesInCWD) {
            String id = sha1((Object) readContents(join(CWD, file)));

            if (!headCommit.hasFile(file) && !index.hasFile(file)) {
                untrackedFiles.add(file);
            }

            if (!index.hasSameFile(file, id)) {
                if (index.hasFile(file)) {
                    modifiedFiles.add(file + " (modified)");
                }
                if (headCommit.hasFile(file) && !headCommit.hasSameFile(file, id)) {
                    modifiedFiles.add(file + " (modified)");
                }
            }
        }

        /* Deleted files: tracked by commit but missing from CWD and not staged for removal */
        for (String file : headCommit.getTrackedFiles().keySet()) {
            if (!filesInCWD.contains(file) && !index.getRemovalSet().contains(file)) {
                modifiedFiles.add(file + " (deleted)");
            }
        }
        for (String file : index.getAdditionMap().keySet()) {
            if (!filesInCWD.contains(file)) {
                modifiedFiles.add(file + " (deleted)");
            }
        }

        output.append("\n=== Modifications Not Staged For Commit ===\n");
        output.append(sortedString(modifiedFiles));
        output.append("\n=== Untracked Files ===\n");
        output.append(sortedString(untrackedFiles)).append("\n");

        return output.toString();
    }

    /** Sort a collection of strings alphabetically and join with newlines. */
    public static String sortedString(Collection<String> collection) {
        String[] items = collection.toArray(new String[0]);
        Arrays.sort(items);
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append("\n");
        }
        return sb.toString();
    }
}
