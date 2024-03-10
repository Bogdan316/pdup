package upt.baker.pdup;

import com.intellij.openapi.vfs.VirtualFile;

public record Dup(VirtualFile firstFile, String firstCode, VirtualFile secondFile, String secondCode) {
}
