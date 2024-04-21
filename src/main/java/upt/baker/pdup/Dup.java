package upt.baker.pdup;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;

public record Dup(VirtualFile firstFile, int firstStart, int firstEnd,
                  VirtualFile secondFile, int secondStart, int secondEnd) {
    public String getFirstCodeSegment() {
        var docManger = FileDocumentManager.getInstance();
        var doc = docManger.getDocument(firstFile);
        if (doc == null) {
            return "";
        }

        return doc.getText(new TextRange(firstStart, firstEnd));
    }

    public String getSecondCodeSegment() {
        var docManger = FileDocumentManager.getInstance();
        var doc = docManger.getDocument(secondFile);
        if (doc == null) {
            return "";
        }

        return doc.getText(new TextRange(secondStart, secondEnd));
    }
}
