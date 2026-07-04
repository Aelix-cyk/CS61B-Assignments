package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (!Repository.initialCheck()) {
                   System.out.println("A Gitlet version-control system already exists in the current directory.");
                } else {
                    Repository.initialize();
                }
                break;
            case "add":
                if (checkArgs(args.length, 2)) {
                    Repository.addToStage(args[1]);
                }
                break;
            case "commit":
                if (checkArgs(args.length, 2)) {
                    if (args[1] == "") {
                        System.out.println("Please enter a commit message.");
                    } else {
                        Repository.createCommit(args[1]);
                    }
                }
                break;
            case "rm":
                if (checkArgs(args.length, 2)) {
                    Repository.removeFromStage(args[1]);
                }
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                if (checkArgs(args.length, 2)) {
                    Repository.findCommit(args[1]);
                }
                break;
            // TODO: FILL THE REST IN
        }
    }

    public static boolean checkArgs(int argsLength, int requiredLength) {
        if (argsLength < requiredLength) {
            System.out.println("Failed for lacking of arguments.");
            return false;
        }
        return true;
    }
}
