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

    /** Create a branch with given name */
    public static void createBranch(String branch, String id) {
        File file = join(REFS_DIR, branch);
        if (fileExists(file)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            writeContents(file, id);
        }
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
               commit = Commit.fromFile(join(Commit.COMMITS_DIR, id));
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
                File file = join(Commit.COMMITS_DIR, id);
                Commit commit = Commit.fromFile(file);
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
                File file = join(Commit.COMMITS_DIR, id);
                String commitMessage = Commit.fromFile(file).getMessage();

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
}
