package gitlet;

import gitlet.storage.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  Uses map-based dispatch to route commands to Repository methods.
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

        Repository repo = new Repository();

        /* Only 'init' is allowed before .gitlet/ exists */
        if (!firstArg.equals("init") && !Persistence.repoExists()) {
            Utils.message("Not in an initialized Gitlet directory.");
            return;
        }

        Map<String, Consumer<String[]>> commands = new HashMap<>();
        commands.put("init",        a -> { checkArgs(a, 1); repo.init(); });
        commands.put("add",         a -> { checkArgs(a, 2); repo.add(a[1]); });
        commands.put("commit",      a -> { checkArgs(a, 2);
                                           if (a[1].isEmpty()) {
                                               Utils.message("Please enter a commit message.");
                                           } else {
                                               repo.commit(a[1]);
                                           } });
        commands.put("rm",          a -> { checkArgs(a, 2); repo.rm(a[1]); });
        commands.put("log",         a -> repo.log());
        commands.put("global-log",  a -> repo.globalLog());
        commands.put("find",        a -> { checkArgs(a, 2); repo.find(a[1]); });
        commands.put("status",      a -> repo.status());
        commands.put("checkout",    a -> dispatchCheckout(repo, a));
        commands.put("branch",      a -> { checkArgs(a, 2); repo.createBranch(a[1]); });
        commands.put("rm-branch",   a -> { checkArgs(a, 2); repo.removeBranch(a[1]); });
        commands.put("reset",       a -> { checkArgs(a, 2); repo.reset(a[1]); });
        commands.put("merge",       a -> { checkArgs(a, 2); repo.merge(a[1]); });

        Consumer<String[]> cmd = commands.getOrDefault(firstArg,
                a -> Utils.message("No command with that name exists."));
        cmd.accept(args);
    }

    /** Dispatch checkout to the correct Repository method based on argument count. */
    private static void dispatchCheckout(Repository repo, String[] args) {
        if (args.length == 2) {
            repo.checkoutBranch(args[1]);
        } else if (args.length == 3 && args[1].equals("--")) {
            repo.checkoutFile(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            repo.checkoutCommitFile(args[1], args[3]);
        } else {
            Utils.message("Incorrect operands.");
        }
    }

    /** Validate the argument count for a command. */
    private static void checkArgs(String[] args, int requiredLength) {
        if (args.length != requiredLength) {
            Utils.message("Incorrect operands.");
            System.exit(0);
        }
    }
}
