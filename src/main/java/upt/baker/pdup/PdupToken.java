package upt.baker.pdup;

public abstract class PdupToken {
    public final int idx;
    public final boolean isId;

    public PdupToken(int idx, boolean isId) {
        this.idx = isId ? idx : -idx;
        this.isId = isId;
    }
}
