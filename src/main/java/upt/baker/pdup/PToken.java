package upt.baker.pdup;

public class PToken extends PdupToken {
    public final int start;
    public final int stop;
    public final String fileName;

    public PToken(int idx, boolean isId, int start, int stop, String fileName) {
        super(idx, isId);
        this.start = start;
        this.stop = stop;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "PToken{" +
                "start=" + start +
                ", stop=" + stop +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
