package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

public class Commit implements Serializable {

    public Commit(String parentHash, String commitMessage,
                  TreeMap<String, String> blobs, Date timeStamp) {
        _parentHashes[0] = parentHash;
        _commitMessage = commitMessage;
        _blobHashes = blobs;
        _timeStamp = timeStamp;
    }

    public Commit(String parentHash1, String parentHash2,
                  String commitMessage, TreeMap<String,
            String> blobs, Date timeStamp) {
        _parentHashes[0] = parentHash1;
        _parentHashes[1] = parentHash2;
        _commitMessage = commitMessage;
        _blobHashes = blobs;
        _timeStamp = timeStamp;
    }

    public String[] getParentHashes() {
        return _parentHashes;
    }

    public String getcommitMessage() {
        return _commitMessage;
    }

    public Date gettimeStamp() {
        return _timeStamp;
    }

    public TreeMap<String, String> getblobHashes() {
        return _blobHashes;
    }

    /** Array of parent hashes. */
    private String[] _parentHashes = new String[2];

    /** Commit message. */
    private String _commitMessage;

    /** Tree map of file names to their hashes. */
    private TreeMap<String, String> _blobHashes;

    /** Commit timestamp. */
    private Date _timeStamp;
}
