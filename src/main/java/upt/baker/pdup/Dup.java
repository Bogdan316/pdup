package upt.baker.pdup;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.PsiUtilCore;

public record Dup(PsiFile firstFile, int firstStart, int firstEnd, PsiFile secondFile, int secondStart,
                  int secondEnd) {
    @Override
    public int firstStart() {
        return PsiUtilCore.getElementAtOffset(firstFile, firstStart).getTextOffset();
    }

    @Override
    public int secondStart() {
        return PsiUtilCore.getElementAtOffset(secondFile, secondStart).getTextOffset();
    }

    @Override
    public int firstEnd() {
        var end = PsiUtilCore.getElementAtOffset(firstFile, firstEnd);
        return end.getTextOffset() + end.getTextLength();
    }

    @Override
    public int secondEnd() {
        var end = PsiUtilCore.getElementAtOffset(secondFile, secondEnd);
        return end.getTextOffset() + end.getTextLength();
    }

    public VirtualFile firstVFile() {
        return firstFile.getVirtualFile();
    }

    public VirtualFile secondVFile() {
        return secondFile.getVirtualFile();
    }

    public String getFirstCodeSegment() {
        var docManger = FileDocumentManager.getInstance();
        var doc = docManger.getDocument(firstFile.getVirtualFile());
        if (doc == null) {
            return "";
        }

        return doc.getText(new TextRange(firstStart(), firstEnd()));
    }

    public String getSecondCodeSegment() {
        var docManger = FileDocumentManager.getInstance();
        var doc = docManger.getDocument(secondFile.getVirtualFile());
        if (doc == null) {
            return "";
        }

        return doc.getText(new TextRange(secondStart(), secondEnd()));
    }
}
