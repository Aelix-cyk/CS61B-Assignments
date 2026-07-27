package gitlet;

public class Blob {
    private final String id;
    private final byte[] contents;

    Blob(byte[] contents) {
        this.contents = contents.clone();
        this.id = Utils.sha1((Object) this.contents);
    }

    public byte[] getContents() {
        return contents.clone();
    }

    public String getId() {
        return id;
    }
}
