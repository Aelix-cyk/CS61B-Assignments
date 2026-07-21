package gitlet.core;

import java.io.Serializable;

import static gitlet.Utils.sha1;

/** Represents a file snapshot (blob) in the Gitlet system.
 *  Immutable value object identified by its content SHA-1.
 *  @author Aelix
 */
public class Blob implements Serializable {

    /** The SHA-1 hash of the blob contents. */
    private final String id;

    /** The raw bytes of the file snapshot. */
    private final byte[] contents;

    /** Creates a new blob from the given byte contents.
     *  The SHA-1 id is computed automatically from the contents.
     */
    public Blob(byte[] contents) {
        this.contents = contents;
        this.id = sha1((Object) contents);
    }

    /** Returns the SHA-1 hex string (40 characters). */
    public String id() {
        return id;
    }

    /** Returns the raw byte contents. */
    public byte[] contents() {
        return contents;
    }

    /** Returns the contents decoded as a UTF-8 string. */
    public String contentsAsString() {
        return new String(contents);
    }
}
