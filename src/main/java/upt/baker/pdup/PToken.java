package upt.baker.pdup;

import com.intellij.openapi.vfs.VirtualFile;

public class PToken extends PdupToken {
    public final int start;
    public final int end;
    public final VirtualFile file;
    public final String str;

    public PToken(int idx, boolean isId, int start, int end, VirtualFile file, String str) {
        super(idx, isId);
        this.start = start;
        this.end = end;
        this.file = file;
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
