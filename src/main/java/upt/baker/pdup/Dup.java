package upt.baker.pdup;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.DocumentUtil;

public record Dup(Document firstDoc, TextRange firstRange, Document secondDoc, TextRange secondRange) {

    private int countLines(String text) {
        if (text == null) {
            return 0;
        }
        int lines = 0;
        boolean onEmptyLine = true;
        final char[] chars = text.toCharArray();
        for (char aChar : chars) {
            if (aChar == '\n' || aChar == '\r') {
                if (!onEmptyLine) {
                    lines++;
                    onEmptyLine = true;
                }
            } else if (aChar != ' ' && aChar != '\t') {
                onEmptyLine = false;
            }
        }
        if (!onEmptyLine) {
            lines++;
        }
        return lines;
    }

    public VirtualFile firstFile() {
        return FileDocumentManager.getInstance().getFile(firstDoc);
    }

    public VirtualFile secondFile() {
        return FileDocumentManager.getInstance().getFile(secondDoc);
    }

    public String getFirstCodeSegment() {
        return firstDoc.getText(firstRange);
    }

    public String getSecondCodeSegment() {
        return secondDoc.getText(secondRange);
    }

    public int firstSegLines() {
        return countLines(getFirstCodeSegment());
    }

    public int secondSegLines() {
        return countLines(getSecondCodeSegment());
    }
}
