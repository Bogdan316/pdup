package upt.baker.pdup;

import com.intellij.openapi.vfs.VirtualFile;

public class PToken extends PdupToken {
    public final int start;
    public final int stop;
    public final VirtualFile file;

    public PToken(int idx, boolean isId, int start, int stop, VirtualFile file) {
        super(idx, isId);
        this.start = start;
        this.stop = stop;
        this.file = file;
    }
}
