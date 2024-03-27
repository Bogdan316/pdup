package upt.baker.pdup;

import com.intellij.openapi.vfs.VirtualFile;

public record Dup(VirtualFile firstFile, String firstCode, int firstStart, int firstEnd,
                  VirtualFile secondFile, String secondCode, int secondStart, int secondEnd) {
}
