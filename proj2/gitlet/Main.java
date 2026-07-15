package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Aelix
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        if (!firstArg.equals("init") && !Repository.GITLET_DIR.exists()) {
            Utils.message("Not in an initialized Gitlet directory.");
            return;
        }
        switch (firstArg) {
            case "init":
                checkArgs(args.length, 1);
                if (!Repository.initialCheck()) {
                    Utils.message("A Gitlet version-control system already exists in the current directory.");
                } else {
                    Repository.initialize();
                }
                break;
            case "add":
                checkArgs(args.length, 2);
                Repository.addToStage(args[1]);
                break;
            case "commit":
                checkArgs(args.length, 2);
                if (args[1].isEmpty()) {
                    Utils.message("Please enter a commit message.");
                } else {
                    Repository.createCommit(args[1]);
                }
                break;
            case "rm":
                checkArgs(args.length, 2);
                Repository.removeFromStage(args[1]);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                checkArgs(args.length, 2);
                Repository.findCommit(args[1]);
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkoutCommitFile(args[1], args[3]);
                } else {
                    Utils.message("Incorrect operands.");
                }
                break;
            case "branch":
                checkArgs(args.length, 2);
                Repository.createBranch(args[1]);
                break;
            case "rm-branch":
                checkArgs(args.length, 2);
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                checkArgs(args.length, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                checkArgs(args.length, 2);
                Repository.merge(args[1]);
                break;
            default:
                Utils.message("No command with that name exists.");
                break;
        }
    }

    public static void checkArgs(int argsLength, int requiredLength) {
        if (argsLength != requiredLength) {
            Utils.message("Incorrect operands.");
            System.exit(0);
        }
    }
}
