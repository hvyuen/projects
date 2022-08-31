package gitlet;



import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;

public class GitletRepo implements Serializable {

    public GitletRepo() {
        stagingArea = new TreeMap<>();
        branches = new TreeMap<>();
        remotes = new TreeMap<>();
        BLOB_FOLDER.mkdir();
        COMMIT_FOLDER.mkdir();
        Commit initialCommit = new Commit("", "initial commit",
                stagingArea, new Date(0));
        byte[] initialCommitSerialized = Utils.serialize(initialCommit);
        String initialCommitSHA = Utils.sha1(initialCommitSerialized);
        File initialCommitFile = Utils.join(COMMIT_FOLDER, initialCommitSHA);
        Utils.writeObject(initialCommitFile, initialCommit);
        branches.put("master", initialCommitSHA);
        head = "master";
    }

    public void add(String fileName) {
        if (!Utils.plainFilenamesIn(Main.CWD).contains(fileName)) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            byte[] fileContents
                    = Utils.readContents(Utils.join(Main.CWD, fileName));
            String fileSHA = Utils.sha1(fileContents);
            if (Utils.readObject(Utils.join(COMMIT_FOLDER, branches.get(head)),
                    Commit.class).getblobHashes().get(fileName) != null
                    && Utils.readObject(Utils.join(
                            COMMIT_FOLDER, branches.get(head)),
                    Commit.class).getblobHashes().
                    get(fileName).equals(fileSHA)) {
                stagingArea.remove(fileName);
            } else {
                stagingArea.put(fileName, fileSHA);
                File copy = Utils.join(BLOB_FOLDER, fileSHA);
                Utils.writeContents(copy, fileContents);
            }
        }
    }

    public void commit(String message, String mergeParent) {
        if (stagingArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else {
            TreeMap<String, String> allFiles
                    = new TreeMap<>(Utils.readObject(Utils.join(COMMIT_FOLDER,
                    branches.get(head)), Commit.class).getblobHashes());
            for (Map.Entry<String, String> entry : stagingArea.entrySet()) {
                if (entry.getValue().equals("")) {
                    allFiles.remove(entry.getKey());
                } else {
                    allFiles.put(entry.getKey(), entry.getValue());
                }
            }
            Commit currentCommit;
            if (mergeParent.equals("")) {
                currentCommit = new Commit(branches.get(head),
                        message, allFiles, new Date());
            } else {
                currentCommit = new Commit(branches.get(head), mergeParent,
                        message, allFiles, new Date());
            }
            byte[] currentCommitSerialized = Utils.serialize(currentCommit);
            String currentCommitSHA = Utils.sha1(currentCommitSerialized);
            File currentCommitFile
                    = Utils.join(COMMIT_FOLDER, currentCommitSHA);
            Utils.writeObject(currentCommitFile, currentCommit);
            branches.put(head, currentCommitSHA);
            stagingArea.clear();
        }
    }

    public void rm(String fileName) {
        if (!stagingArea.containsKey(fileName)
                && !Utils.readObject(Utils.join(
                        COMMIT_FOLDER, branches.get(head)),
                Commit.class).getblobHashes().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        } else {
            stagingArea.remove(fileName);
            if (Utils.readObject(Utils.join(
                    COMMIT_FOLDER, branches.get(head)),
                    Commit.class).getblobHashes().containsKey(fileName)) {
                stagingArea.put(fileName, "");
                Utils.restrictedDelete(Utils.join(Main.CWD, fileName));
            }
        }
    }

    public void checkout(String commitID, String fileName) {
        String fullCommitID = "";
        for (String commitName : Utils.plainFilenamesIn(COMMIT_FOLDER)) {
            if (commitID.equals(
                    commitName.substring(0, commitID.length()))) {
                fullCommitID = commitName;
                break;
            }
        }
        if (fullCommitID.equals("")) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit checkoutCommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                fullCommitID), Commit.class);
        if (!checkoutCommit.getblobHashes().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            File checkoutFile = Utils.join(BLOB_FOLDER,
                    checkoutCommit.getblobHashes().get(fileName));
            Utils.writeContents(Utils.join(Main.CWD, fileName),
                    Utils.readContents(checkoutFile));
        }
    }

    public void checkout(String fileName) {
        checkout(branches.get(head), fileName);
    }

    public void checkoutBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (branchName.equals(head)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        } else {
            TreeMap<String, String> branchCommitBlobs
                    = Utils.readObject(Utils.join(
                            COMMIT_FOLDER, branches.get(branchName)),
                    Commit.class).getblobHashes();
            for (String fileName : Utils.plainFilenamesIn(Main.CWD)) {
                if (!Utils.readObject(Utils.join(
                        COMMIT_FOLDER, branches.get(head)),
                        Commit.class).getblobHashes().containsKey(fileName)
                        && branchCommitBlobs.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            for (Map.Entry<String, String> entry
                    : branchCommitBlobs.entrySet()) {
                Utils.writeContents(Utils.join(Main.CWD, entry.getKey()),
                        Utils.readContents(Utils.join(
                                BLOB_FOLDER, entry.getValue())));
            }
            for (String fileName : Utils.plainFilenamesIn(Main.CWD)) {
                if (!branchCommitBlobs.containsKey(fileName)) {
                    Utils.restrictedDelete(Utils.join(Main.CWD, fileName));
                }
            }
            head = branchName;
            stagingArea.clear();
        }
    }

    public void log() {
        String pointer = branches.get(head);
        while (!pointer.equals("")) {
            System.out.println("===");
            System.out.println("commit " + pointer);
            Commit current = Utils.readObject(
                    Utils.join(COMMIT_FOLDER, pointer), Commit.class);
            if (current.getParentHashes()[1] != null) {
                System.out.println("Merge: "
                        + current.getParentHashes()[0].substring(0, 7)
                        + " " + current.getParentHashes()[1].substring(0, 7));
            }
            Date commitDate = current.gettimeStamp();
            String formattedDate
                    = String.format("%ta %tb %td %tT %tY %tz", commitDate,
                    commitDate, commitDate, commitDate, commitDate, commitDate);
            System.out.println("Date: " + formattedDate);
            System.out.println(current.getcommitMessage());
            System.out.println();
            pointer = current.getParentHashes()[0];
        }
    }

    public void globalLog() {
        for (String commit : Utils.plainFilenamesIn(COMMIT_FOLDER)) {
            System.out.println("===");
            System.out.println("commit " + commit);
            Commit current = Utils.readObject(
                    Utils.join(COMMIT_FOLDER, commit), Commit.class);
            Date commitDate = current.gettimeStamp();
            String formattedDate = String.format("%ta %tb %td %tT %tY %tz",
                    commitDate, commitDate,
                    commitDate, commitDate, commitDate, commitDate);
            System.out.println("Date: " + formattedDate);
            System.out.println(current.getcommitMessage());
            System.out.println();
        }
    }

    public void find(String message) {
        boolean found = false;
        for (String commitHash : Utils.plainFilenamesIn(COMMIT_FOLDER)) {
            Commit current = Utils.readObject(
                    Utils.join(COMMIT_FOLDER,
                            commitHash), Commit.class);
            if (current.getcommitMessage().equals(message)) {
                System.out.println(commitHash);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public void status() {
        System.out.println("=== Branches ===");
        for (Map.Entry<String, String> entry : branches.entrySet()) {
            if (entry.getKey().equals(head)) {
                System.out.println("*" + entry.getKey());
            } else {
                System.out.println(entry.getKey());
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (Map.Entry<String, String> entry : stagingArea.entrySet()) {
            if (!entry.getValue().equals("")) {
                System.out.println(entry.getKey());
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (Map.Entry<String, String> entry : stagingArea.entrySet()) {
            if (entry.getValue().equals("")) {
                System.out.println(entry.getKey());
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            branches.put(branchName, branches.get(head));
        }
    }

    public void rmBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (branchName.equals(head)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            branches.remove(branchName);
        }
    }

    public void reset(String commitID) {
        String fullCommitID = "";
        for (String commitName : Utils.plainFilenamesIn(COMMIT_FOLDER)) {
            if (commitID.equals(commitName.substring(0, commitID.length()))) {
                fullCommitID = commitName;
                break;
            }
        }
        if (fullCommitID.equals("")) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            TreeMap<String, String> commitBlobs = Utils.readObject(
                    Utils.join(COMMIT_FOLDER,
                            fullCommitID), Commit.class).getblobHashes();
            for (String fileName : Utils.plainFilenamesIn(Main.CWD)) {
                if (!Utils.readObject(Utils.join(COMMIT_FOLDER,
                        branches.get(head)),
                        Commit.class).getblobHashes().containsKey(fileName)
                        && commitBlobs.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            for (Map.Entry<String, String> entry : commitBlobs.entrySet()) {
                Utils.writeContents(Utils.join(Main.CWD, entry.getKey()),
                        Utils.readContents(
                                Utils.join(BLOB_FOLDER, entry.getValue())));
            }
            for (String fileName : Utils.plainFilenamesIn(Main.CWD)) {
                if (!commitBlobs.containsKey(fileName)) {
                    Utils.restrictedDelete(Utils.join(Main.CWD, fileName));
                }
            }
            branches.put(head, commitID);
            stagingArea.clear();
        }
    }

    public void merge(String branchName) {
        failureCases(branchName);
        Commit current = Utils.readObject(Utils.join(
                COMMIT_FOLDER, branches.get(head)), Commit.class);
        Commit given = Utils.readObject(Utils.join(
                COMMIT_FOLDER, branches.get(branchName)), Commit.class);
        String lca = leastCommonAncestor(
                branches.get(head), branches.get(branchName));
        failureCases2(branchName, lca);
        Commit lcaCommit = Utils.readObject(
                Utils.join(COMMIT_FOLDER, lca), Commit.class);
        TreeMap<String, String> currentBlobs = current.getblobHashes();
        TreeMap<String, String> givenBlobs = given.getblobHashes();
        TreeMap<String, String> lcaBlobs = lcaCommit.getblobHashes();
        ArrayList<String> allFiles = new ArrayList<>();
        for (String fileName : Utils.plainFilenamesIn(Main.CWD)) {
            allFiles.add(fileName);
        }
        for (Map.Entry<String, String> entry : givenBlobs.entrySet()) {
            if (!allFiles.contains(entry.getKey())) {
                allFiles.add(entry.getKey());
            }
        }
        boolean isConflict =
                helper3(allFiles, currentBlobs, givenBlobs, lcaBlobs);
        writingFiles(branchName, currentBlobs, isConflict);
    }


    private void failureCases(String branchName) {
        if (!stagingArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (branchName.equals(head)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    private void failureCases2(String branchName, String lca) {
        if (lca.equals(branches.get(branchName))) {
            System.out.println("Given branch is an"
                    + " ancestor of the current branch.");
            System.exit(0);
        } else if (lca.equals(branches.get(head))) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    private boolean helper3(ArrayList<String> allFiles,
                            TreeMap<String, String> currentBlobs,
                         TreeMap<String, String> givenBlobs,
                            TreeMap<String, String> lcaBlobs) {
        boolean isConflict = false;
        for (String fileName : allFiles) {
            if (!lcaBlobs.containsKey(fileName)
                    && !currentBlobs.containsKey(fileName)
                    && givenBlobs.containsKey(fileName)) {
                stagingArea.put(fileName, givenBlobs.get(fileName));
            } else if (lcaBlobs.containsKey(fileName)
                    && currentBlobs.containsKey(fileName)
                    && lcaBlobs.get(fileName).
                    equals(currentBlobs.get(fileName))) {
                if (givenBlobs.containsKey(fileName)
                        && !givenBlobs.get(fileName).equals(
                        currentBlobs.get(fileName))) {
                    stagingArea.put(fileName, givenBlobs.get(fileName));
                } else if (!givenBlobs.containsKey(fileName)) {
                    stagingArea.put(fileName, "");
                    Utils.restrictedDelete(Utils.join(Main.CWD, fileName));
                }
            } else if ((currentBlobs.containsKey(fileName) && givenBlobs
                    .containsKey(fileName) && !currentBlobs.get(fileName)
                    .equals(givenBlobs.get(fileName)) && (!lcaBlobs
                    .containsKey(fileName) || !lcaBlobs.get(fileName)
                    .equals(givenBlobs.get(fileName)))) || (lcaBlobs
                    .containsKey(fileName) && currentBlobs.containsKey(
                            fileName) && !givenBlobs.containsKey(fileName)
                    && !lcaBlobs.get(fileName).equals(currentBlobs
                    .get(fileName))) || (lcaBlobs.containsKey(fileName)
                    && !currentBlobs.containsKey(fileName) && givenBlobs
                    .containsKey(fileName) && !lcaBlobs.get(fileName)
                    .equals(givenBlobs.get(fileName)))) {
                String currentFile;
                String givenFile;
                isConflict = true;
                if (!currentBlobs.containsKey(fileName)) {
                    currentFile = "";
                } else {
                    currentFile = Utils.readContentsAsString(
                            Utils.join(BLOB_FOLDER,
                                    currentBlobs.get(fileName)));
                }
                if (!givenBlobs.containsKey(fileName)) {
                    givenFile = "";
                } else {
                    givenFile = Utils.readContentsAsString(Utils.join(
                            BLOB_FOLDER, givenBlobs.get(fileName)));
                }
                String combinedContents
                        = combineContents(currentFile, givenFile);
                String fileSHA = Utils.sha1(combinedContents);
                stagingArea.put(fileName, fileSHA);
                File copy = Utils.join(BLOB_FOLDER, fileSHA);
                Utils.writeContents(copy, combinedContents);
            }
        }
        return isConflict;
    }

    private void writingFiles(String branchName,
                              TreeMap<String, String> currentBlobs,
                              boolean isConflict) {
        ArrayList<String> allFiles2 = new ArrayList<>();
        for (String fileName : Utils.plainFilenamesIn(Main.CWD)) {
            allFiles2.add(fileName);
        }
        for (Map.Entry<String, String> entry : stagingArea.entrySet()) {
            if (!currentBlobs.containsKey(entry.getKey())
                    && allFiles2.contains(entry.getKey())) {
                System.out.println("There is an "
                        + "untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (Map.Entry<String, String> entry : stagingArea.entrySet()) {
            if (!entry.getValue().equals("")) {
                Utils.writeContents(Utils.join(Main.CWD, entry.getKey()),
                        Utils.readContents(Utils.join(
                                BLOB_FOLDER, entry.getValue())));
            }
        }
        commit("Merged " + branchName + " into " + head
                + ".", branches.get(branchName));
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private String combineContents(String currentFile, String givenFile) {
        return "<<<<<<< HEAD\n"
                + currentFile
                + "=======\n"
                + givenFile
                + ">>>>>>>\n";
    }

    private String leastCommonAncestor(String currentHash, String givenHash) {
        int minDistance = Integer.MAX_VALUE;
        String lcaHash = "";
        for (String commitHash : Utils.plainFilenamesIn(COMMIT_FOLDER)) {
            int leastDistanceToCurrent = leastDistance(currentHash, commitHash);
            if (minDistance > leastDistanceToCurrent
                    && leastDistance(givenHash,
                    commitHash) != Integer.MAX_VALUE) {
                minDistance = leastDistanceToCurrent;
                lcaHash = commitHash;
            }
        }
        return lcaHash;
    }

    private int leastDistance(String currentHash, String commitHash) {
        Commit current = Utils.readObject(
                Utils.join(COMMIT_FOLDER, currentHash), Commit.class);
        if (currentHash.equals(commitHash)) {
            return 0;
        } else if (current.getParentHashes()[0].equals("")) {
            return Integer.MAX_VALUE;
        } else if (current.getParentHashes()[1] == null) {
            int temp = leastDistance(current.getParentHashes()[0], commitHash);
            if (temp < Integer.MAX_VALUE) {
                return 1 + temp;
            } else {
                return temp;
            }
        } else {
            int temp = Integer.min(
                    leastDistance(current.getParentHashes()[0], commitHash),
                    leastDistance(current.getParentHashes()[1], commitHash));
            if (temp < Integer.MAX_VALUE) {
                return 1 + temp;
            } else {
                return temp;
            }
        }
    }

    public void addRemote(String remoteName, String remoteDirectoryName) {
        if (getRemotes().containsKey(remoteName)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        } else {
            getRemotes().put(remoteName, remoteDirectoryName);
        }
    }

    public void rmRemote(String remoteName) {
        if (!getRemotes().containsKey(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        } else {
            getRemotes().remove(remoteName);
        }
    }

    public void push(String remoteName, String remoteBranchName) {
        File remoteGitletFolder = Utils.join(getRemotes().get(remoteName));
        if (!remoteGitletFolder.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        } else {
            GitletRepo remoteGitlet
                    = Utils.readObject(Utils.join(remoteGitletFolder,
                    "gitlet_repo_file"), GitletRepo.class);
            if (!remoteGitlet.getBranches().containsKey(remoteBranchName)) {
                remoteGitlet.branch(remoteBranchName);
            }
            if (leastDistance(branches.get(head), remoteGitlet.getBranches().
                    get(remoteBranchName)) == Integer.MAX_VALUE) {
                System.out.println(
                        "Please pull down remote changes before pushing.");
                System.exit(0);
            } else {
                String currentHash = branches.get(head);
                String givenHash
                        = remoteGitlet.getBranches().get(remoteBranchName);
                while (!currentHash.equals(givenHash)) {
                    Commit current = Utils.readObject(
                            Utils.join(COMMIT_FOLDER,
                                    currentHash), Commit.class);
                    for (Map.Entry<String, String> entry
                            : current.getblobHashes().entrySet()) {
                        Utils.writeContents(Utils.join(
                                Utils.join(remoteGitletFolder,
                                "blobs"), entry.getValue()), Utils.readContents
                                (Utils.join(BLOB_FOLDER, entry.getValue())));
                    }
                    Utils.writeContents(Utils.join(
                            Utils.join(remoteGitletFolder,
                            "commits"), currentHash), Utils.readContents(
                                    Utils.join(COMMIT_FOLDER, currentHash)));
                    currentHash = current.getParentHashes()[0];
                }
                remoteGitlet.getBranches().put(
                        remoteGitlet.getHead(), branches.get(head));
                Utils.writeObject(Utils.join(remoteGitletFolder,
                        "gitlet_repo_file"), remoteGitlet);
            }
        }
    }

    public void fetch(String remoteName, String remoteBranchName) {
        File remoteGitletFolder = Utils.join(getRemotes().get(remoteName));
        if (!remoteGitletFolder.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        } else {
            GitletRepo remoteGitlet
                    = Utils.readObject(Utils.join(remoteGitletFolder,
                    "gitlet_repo_file"), GitletRepo.class);
            if (!remoteGitlet.getBranches().containsKey(remoteBranchName)) {
                System.out.println("That remote does not have that branch.");
                System.exit(0);
            } else {
                String givenHash
                        = remoteGitlet.getBranches().get(remoteBranchName);
                while (!Utils.join(COMMIT_FOLDER,
                        givenHash).exists() && !givenHash.equals("")) {
                    Commit current = Utils.readObject(
                            Utils.join(Utils.join(remoteGitletFolder,
                                    "commits"), givenHash), Commit.class);
                    for (Map.Entry<String, String> entry
                            : current.getblobHashes().entrySet()) {
                        Utils.writeContents(Utils.join(
                                BLOB_FOLDER, entry.getValue()),
                                Utils.readContents(Utils.join(
                                        Utils.join(remoteGitletFolder,
                                        "blobs"), entry.getValue())));
                    }
                    Utils.writeContents(Utils.join(COMMIT_FOLDER, givenHash),
                            Utils.readContents(Utils.join(Utils.join(
                                    remoteGitletFolder,
                                    "commits"), givenHash)));
                    givenHash = current.getParentHashes()[0];
                }
                if (!branches.containsKey(remoteName
                        + File.separator + remoteBranchName)) {
                    branch(remoteName + File.separator + remoteBranchName);
                }
                branches.put(remoteName + File.separator
                        + remoteBranchName,
                        remoteGitlet.getBranches().get(remoteGitlet.getHead()));
            }
        }
    }

    public void pull(String remoteName, String remoteBranchName) {
        fetch(remoteName, remoteBranchName);
        merge(remoteName + File.separator + remoteBranchName);
    }

    public TreeMap<String, String> getRemotes() {
        return remotes;
    }

    public TreeMap<String, String> getStagingArea() {
        return stagingArea;
    }

    public TreeMap<String, String> getBranches() {
        return branches;
    }

    public String getHead() {
        return head;
    }

    /** TreeMap mapping the remote names to their branch names. */
    private TreeMap<String, String> remotes;

    /** TreeMap representing the staging area. */
    private TreeMap<String, String> stagingArea;

    /** TreeMap mapping the names of branches to their hashes. */
    private TreeMap<String, String> branches;

    /** The name (not the hash) of the current
     *  head which points to the current commit. */
    private String head;

    /** Folder containing the blobs. */
    static final File BLOB_FOLDER = Utils.join(Main.GITLET_FOLDER, "blobs");

    /** Folder containing the commits. */
    static final File COMMIT_FOLDER = Utils.join(Main.GITLET_FOLDER, "commits");
}
