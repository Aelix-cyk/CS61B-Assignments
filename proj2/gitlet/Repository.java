package gitlet;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  It contains the main logic of gitlet.
 *  @author TODO
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The head file */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    /** Setup files and directories for persistence */
    public static void setupPersistence() {
        boolean result;

        result = GITLET_DIR.mkdir()
                | BLOBS_DIR.mkdir()
                | Commit.COMMITS_DIR.mkdir()
                | REFS_DIR.mkdir();
        if (!result) {
            System.out.println("setupPersistence failed!");
            System.exit(0);
        }

        Stage.saveStage(new Stage());
    }

    /** Check if there is a gitlet system already */
    public static boolean initialCheck() {
        return !directoryExists(GITLET_DIR.getName());
    }

    /** Initial commit */
    public static void initialize() {
        String defaultBranch = "master";
        Commit initialCommit = new Commit("initial commit");

        setupPersistence();
        initialCommit.setEpochTime();
        String id = Commit.saveCommit(initialCommit);
        createBranch(defaultBranch, id);
        setHead(defaultBranch);
    }

    /** Create a branch with given name and commit id */
    public static void createBranch(String branch, String id) {
        File file = join(REFS_DIR, branch);
        if (fileExists(file)) {
            System.out.println("A branch with that name already exists.");
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
            System.out.println("A branch with that name does not exist.");
        } else if (branch.equals(getHeadName())) {
            System.out.println("Cannot remove the current branch.");
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
    public static void setHeadCommitId (String id) {
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
        return readObject(join(Commit.COMMITS_DIR, getHeadCommitId()), Commit.class);
    }

    /** Get the branch name of current HEAD */
    public static String getHeadName() {
        Pattern pattern = Pattern.compile("refs/(?<branchName>\\w+)");
        Matcher matcher = pattern.matcher(readContentsAsString(HEAD_FILE));
        if (matcher.find()) {
            return matcher.group("branchName");
        } else {
            System.out.println("Fail to get Head's branch name");
            System.exit(0);
            return "";
        }
    }

    /** Get the commit object that the branch point to */
    public static Commit getBranchCommit(String branch) {
        return readObject(join(Commit.COMMITS_DIR, getBranchCommitId(branch)), Commit.class);
    }

    /** Get the branch's commit id */
    public static String getBranchCommitId(String branch) {
        return readContentsAsString(join(REFS_DIR, branch));
    }

    /** Add file to staging area */
    public static void addToStage(String name) {
        Stage stage = Stage.fromFile();
        stage.addFile(join(CWD, name), getHeadCommit());
        Stage.saveStage(stage);
    }

    /** Remove file from staging area, add it to stage for removal */
    public static void removeFromStage(String name) {
        Stage stage = Stage.fromFile();
        stage.removeFile(join(CWD, name), getHeadCommit());
        Stage.saveStage(stage);
    }

    /** Write blob into file */
    public static void saveBlob(String id, byte[] contents) {
        File blob = join(BLOBS_DIR, id);
        writeContents(blob, contents);
    }

    /** Create a new commit and move HEAD pointer to it */
    public static void createCommit(String message) {
        Stage stage = Stage.fromFile();

        if (stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit parentCommit = getHeadCommit();
        Commit newCommit = new Commit(message, parentCommit, getHeadCommitId());

        /* process files in staging area */
        newCommit.addFromMap(stage.getAdditionMap());
        newCommit.removeFromSet(stage.getRemovalSet());
        stage.clear();
        Stage.saveStage(stage);

        /* save commit and update HEAD pointer */
        String id = Commit.saveCommit(newCommit);
        setHeadCommitId(id);
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
               commit = Commit.fromFile(id);
           } else {
               break;
           }
       }
       System.out.print(log.toString());
    }

    /** Print global commit log */
    public static void globalLog() {
        List<String> fileList = plainFilenamesIn(Commit.COMMITS_DIR);
        StringBuilder log = new StringBuilder();

        if (fileList != null) {
            for (String id : fileList) {
                Commit commit = Commit.fromFile(id);
                log.append(commit.dumpLog(id));
            }
            System.out.print(log.toString());
        }
    }

    /** Find commits with given commit message */
    public static void findCommit(String message) {
        List<String> fileList = plainFilenamesIn(Commit.COMMITS_DIR);
        StringBuilder log = new StringBuilder();

        if (fileList != null) {
            for (String id : fileList) {
                String commitMessage = Commit.fromFile(id).getMessage();

                if (commitMessage.contains(message)) {
                    log.append(id);
                    log.append("\n");
                }
            }

            if (log.length() > 0) {
                System.out.print(log.toString());
            } else {
                System.out.println("Found no commit with that message.");
            }
        }
    }

    /** Show the status of gitlet system */
    public static void status() {
        StringBuilder output = new StringBuilder();

        /* status of branches */
        ArrayList<String> branchNames = new ArrayList<>(plainFilenamesIn(REFS_DIR));
        String currentBranch = getHeadName();

        output.append("=== Branches ===\n");
        branchNames.remove(currentBranch);
        branchNames.add("*" + currentBranch);
        output.append(sortedStatusString(branchNames));

        /* status of staged files and removed files */
        Stage stage = Stage.fromFile();
        Map<String, String> additionMap = stage.getAdditionMap();
        Set<String> removalSet = stage.getRemovalSet();

        output.append("\n=== Staged Files ===\n");
        output.append(sortedStatusString(additionMap.keySet()));
        output.append("\n=== Removed Files ===\n");
        output.append(sortedStatusString(removalSet));

        /* status of modifications that are not staged for commit */
        List<String> filesInCWD = plainFilenamesIn(CWD);
        Commit currentCommit = getHeadCommit();
        Map<String, String> commitMap = currentCommit.getTrackedFiles();
        HashSet<String> modifiedFiles = new HashSet<>();
        HashSet<String> untrackedFiles = new HashSet<>();
        if (filesInCWD != null) {
            /* traverse files in current directory */
            for (String file : filesInCWD) {
                String id = sha1(readContents(join(CWD, file)));
                /* not in current commit and not in additionMap */
                if (!currentCommit.hasFile(file) && !stage.hasFile(file)) untrackedFiles.add(file);

                if (!stage.hasSameFile(file, id)) {
                    /* staged for addition but changed */
                    if (stage.hasFile(file)) modifiedFiles.add(file + " (modified)");
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
                if (!filesInCWD.contains(file)) modifiedFiles.add(file + " (deleted)");
            }
        }

        output.append("\n=== Modifications Not Staged For Commit ===\n");
        output.append(sortedStatusString(modifiedFiles));
        output.append("\n=== Untracked Files ===\n");
        output.append(sortedStatusString(untrackedFiles)).append("\n");

        System.out.print(output.toString());
    }

    public static String sortedStatusString(Collection<String> collection) {
        String[] files = collection.toArray(new String[0]);
        Arrays.sort(files);
        StringBuilder sorted = new StringBuilder();
        for (String file : files) sorted.append(file).append("\n");
        return sorted.toString();
    }

    /** Check out the given file in previous commit */
    public static void checkoutFile(String file) {
        Commit commit = getHeadCommit();
        if (!commit.hasFile(file)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        restoreFile(file, commit.trackedFileId(file));
    }

    /** Check out the given file in given commit */
    public static void checkoutCommitFile(String shortId, String file) {
        String id = getLongId(shortId);
        Commit commit = Commit.fromFile(id);
        if (commit.hasFile(file)) restoreFile(file, commit.trackedFileId(file));
        else System.out.println("File does not exist in that commit.");
    }

    /** Check out the given branch */
    public static void checkoutBranch(String branch) {
        checkBranchExists(branch);
        /* check that branch is not current branch */
        String currentBranch = getHeadName();
        if (branch.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        checkoutCommit(getBranchCommitId(branch));
        setHead(branch);
    }

    /** Check the given branch exists or not */
    public static void checkBranchExists(String branch) {
        ArrayList<String> branchNames = new ArrayList<>(Objects.requireNonNull(plainFilenamesIn(REFS_DIR)));
        if (!branchNames.contains(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
    }

    /** Checkout the given commit */
    public static void checkoutCommit(String id) {
        /* check untracked files that would be overwritten */
        ArrayList<String> files = new ArrayList<>(Objects.requireNonNull(plainFilenamesIn(CWD)));
        Commit currentCommit = getHeadCommit();
        Commit checkOutCommit = Commit.fromFile(id);
        Map<String, String> checkOutTrackedFiles = checkOutCommit.getTrackedFiles();
        HashSet<String> filesToOverwrite = new HashSet<>();
        HashSet<String> filesToDelete = new HashSet<>();
        HashSet<String> filesToCreate = new HashSet<>();

        for (String file : checkOutTrackedFiles.keySet()) {
            if (files.contains(file)) {
                if (!currentCommit.hasFile(file) && checkOutCommit.hasFile(file)) printUntrackedFileError();
                else if (checkOutCommit.hasFile(file)) filesToOverwrite.add(file);
                else filesToDelete.add(file);
            } else {
                filesToCreate.add(file);
            }
        }

        for (String file : filesToDelete) restrictedDelete(join(CWD, file));
        for (String file : filesToCreate) restoreFile(file, checkOutTrackedFiles.get(file));
        for (String file : filesToOverwrite) {
            String currentId = sha1((Object) readContents(join(CWD, file)));
            String checkOutId = checkOutTrackedFiles.get(file);
            if (!currentId.equals(checkOutId)) restoreFile(file, checkOutId);
        }
        Stage.saveStage(new Stage());
    }

    /** Print error message for untranced files */
    public static void printUntrackedFileError() {
        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
        System.exit(0);
    }

    /** Restore given file from blobs by sha-1 id */
    public static void restoreFile(String file, String id) {
        writeContents(join(CWD, file), (Object) readContents(join(BLOBS_DIR, id)));
    }

    /** Return the long id with its first 6 or more digits if it exists */
    public static String getLongId(String id) {
        if (id.length() == UID_LENGTH) return id;
        ArrayList<String> files = new ArrayList<>(Objects.requireNonNull(plainFilenamesIn(Commit.COMMITS_DIR)));
        for (String file : files) {
            if (file.substring(0, id.length() - 1).equals(id)) return file;
        }
        System.out.println("No commit with that id exists.");
        System.exit(0);
        return "";
    }

    /** Reset with given commit id */
    public static void reset(String shordId) {
        String id = getLongId(shordId);
        checkoutCommit(id);
        setHeadCommitId(id);
    }

    /** Merge the files in given branch to current branch */
    public static void merge(String branch) {
        Stage stage = Stage.fromFile();

        /* check error cases */
        if (!stage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        checkBranchExists(branch);
        if (branch.equals(getHeadName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        String splitPointId = findSplitPoint(getHeadName(), branch);

        /* check special cases */
        if (splitPointId.equals(getBranchCommitId(branch))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPointId.equals(getHeadCommitId())) {
            checkoutBranch(branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        /* check untracked files that would be overwritten */
        ArrayList<String> filesInCWD = new ArrayList<>(Objects.requireNonNull(plainFilenamesIn(CWD)));
        Commit splitCommit = Commit.fromFile(splitPointId);
        Commit currentCommit = getHeadCommit();
        Commit givenCommit = getBranchCommit(branch);
        HashMap<String, String> splitMap = new HashMap<>(splitCommit.getTrackedFiles());
        HashMap<String, String> currentMap = new HashMap<>(currentCommit.getTrackedFiles());
        HashMap<String, String> givenMap = new HashMap<>(givenCommit.getTrackedFiles());
        // files to be checked out
        HashSet<String> checkOutSet = new HashSet<>();
        // files to be removed
        HashSet<String> removeSet = new HashSet<>();
        // files that are conflicted
        HashSet<String> conflictSet = new HashSet<>();
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
                    if (!currentId.equals(givenId)) checkOutSet.add(file);
                } else {
                    removeSet.add(file);
                }
            } else if (currentMap.containsKey(file) && !currentId.equals((splitId))) {
                /* current file is modified */
                if (!givenMap.containsKey(file)) {
                    /* given file is removed */
                    conflictSet.add(file);
                } else if (!givenId.equals(currentId) && !givenId.equals(splitId)) {
                    /* given file is modified in different way */
                    conflictSet.add(file);
                }

            } else {
                /* current file is deleted */
                if (givenMap.containsKey(file) && !givenId.equals(splitId)) {
                    /* check untracked files that would be overwritten */
                    if (filesInCWD.contains(file)) printUntrackedFileError();
                    /* given file is modified in different way */
                    conflictSet.add(file);
                }
            }
        }

        /* files in given branch, but not in split point */
        for (String file : givenRemainSet) {
            if (!currentRemainSet.contains(file)) {
                if (filesInCWD.contains(file)) printUntrackedFileError();
                checkOutSet.add(file);
            }
            else conflictSet.add(file);
        }

        /* check out files in checkOutSet */
        for (String file : checkOutSet) {
            restoreFile(file, givenCommit.trackedFileId(file));
            stage.addFile(join(CWD, file), currentCommit);
        }
        /* remove files in removeSet */
        for (String file : removeSet) stage.removeFile(join(CWD, file), currentCommit);
        /* update files in conflictSet */
        for (String file : conflictSet) {
            writeConflictFile(file, currentCommit, givenCommit);
            stage.addFile(join(CWD, file), currentCommit);
        }
    }

    /** Write the conflict file with special format */
    public static void writeConflictFile(String file, Commit current, Commit given) {
        byte[] currentContents, givenContents;
        if (current.hasFile(file)) currentContents = readContents(join(BLOBS_DIR, current.trackedFileId(file)));
        else currentContents = new byte[]{};
        if (given.hasFile(file)) givenContents = readContents(join(BLOBS_DIR, given.trackedFileId(file)));
        else givenContents = new byte[]{};

        writeContents(join(CWD, file), "<<<<<<< HEAD\n", currentContents, "=======\n", givenContents, ">>>>>>>\n");
    }

    /** Find the split point between two branch */
    public static String findSplitPoint(String branchA, String branchB) {
        String[] branches = new String[]{branchA, branchB};
        HashSet<String>[] commitsSet = new HashSet[]{new HashSet<String>(), new HashSet<String>()};
        ArrayDeque<String>[] commitsDeque = new ArrayDeque[]{new ArrayDeque<String>(), new ArrayDeque<String>()};
        for (int i = 0; i < commitsSet.length; i += 1) {
            commitsDeque[i].addLast(getBranchCommitId(branches[i]));
            commitsSet[i].add(commitsDeque[i].getFirst());
        }

        while (true) {
            for (int i = 0; i < commitsSet.length; i += 1) {
                ArrayDeque<String> tempDeque = new ArrayDeque<>(commitsDeque[i]);
                commitsDeque[i].clear();
                for (String id : tempDeque) {
                    if (commitsSet[1-i].contains(id)) return id;
                    commitsSet[i].add(id);
                    Commit commit = Commit.fromFile(id);
                    commitsDeque[i].addLast(commit.getParent());
                    if (commit.getSecondParent() != null) commitsDeque[i].addLast(commit.getSecondParent());
                }
            }
        }
    }
}
