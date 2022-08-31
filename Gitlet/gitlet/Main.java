package gitlet;


import java.io.File;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Har Vey Yuen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (args[0].equals("init")) {
            if (GITLET_FOLDER.exists()) {
                System.out.println("A Gitlet version-control system "
                        + "already exists in the current directory.");
                System.exit(0);
            } else {
                GITLET_FOLDER.mkdir();
                _gitletRepo = new GitletRepo();
                Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            }
        } else {
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                System.exit(0);
            } else {
                switchStatement(args);
            }
        }
    }

    private static void switchStatement(String[] args) {
        _gitletRepo = Utils.readObject(
                GITLET_REPO_FILE, GitletRepo.class);
        switch (args[0]) {
        case "add":
            checkNumArgs(args, 2);
            _gitletRepo.add(args[1]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "commit":
            checkNumArgs(args, 2);
            _gitletRepo.commit(args[1], "");
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "rm":
            checkNumArgs(args, 2);
            _gitletRepo.rm(args[1]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "checkout":
            checkout(args);
            break;
        case "log":
            checkNumArgs(args, 1);
            _gitletRepo.log();
            break;
        case "global-log":
            checkNumArgs(args, 1);
            _gitletRepo.globalLog();
            break;
        case "find":
            checkNumArgs(args, 2);
            _gitletRepo.find(args[1]);
            break;
        case "status":
            checkNumArgs(args, 1);
            _gitletRepo.status();
            break;
        case "branch":
            checkNumArgs(args, 2);
            _gitletRepo.branch(args[1]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "rm-branch":
            checkNumArgs(args, 2);
            _gitletRepo.rmBranch(args[1]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "reset":
            reset(args);
            break;
        case "merge":
            merge(args);
            break;
        default:
            remoteCommands(args);
        }
    }

    private static void remoteCommands(String[] args) {
        switch (args[0]) {
        case "add-remote":
            checkNumArgs(args, 3);
            _gitletRepo.addRemote(args[1], args[2]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "rm-remote":
            checkNumArgs(args, 2);
            _gitletRepo.rmRemote(args[1]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "push":
            checkNumArgs(args, 3);
            _gitletRepo.push(args[1], args[2]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "fetch":
            checkNumArgs(args, 3);
            _gitletRepo.fetch(args[1], args[2]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        case "pull":
            checkNumArgs(args, 3);
            _gitletRepo.pull(args[1], args[2]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

    private static void reset(String[] args) {
        checkNumArgs(args, 2);
        _gitletRepo.reset(args[1]);
        Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
    }

    private static void merge(String[] args) {
        checkNumArgs(args, 2);
        _gitletRepo.merge(args[1]);
        Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
    }

    private static void checkout(String[] args) {
        if (args.length == 3 && args[1].equals("--")) {
            _gitletRepo.checkout(args[2]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
        } else if (args.length == 4 && args[2].equals("--")) {
            _gitletRepo.checkout(args[1], args[3]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
        } else if (args.length == 2) {
            _gitletRepo.checkoutBranch(args[1]);
            Utils.writeObject(GITLET_REPO_FILE, _gitletRepo);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void checkNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static GitletRepo getGitletRepo() {
        return _gitletRepo;
    }

    /** Gitlet repo object. */
    private static GitletRepo _gitletRepo;

    /** Current Working Directory. */
    static final File CWD = new File(".");

    /** Gitlet folder. */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    /** File representing the gitlet repo. */
    static final File GITLET_REPO_FILE
            = Utils.join(GITLET_FOLDER, "gitlet_repo_file");
}
