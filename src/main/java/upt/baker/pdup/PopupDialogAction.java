package upt.baker.pdup;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.RegisterToolWindowTaskBuilder;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.ui.components.JBPanel;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class PopupDialogAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private final Map<CharSequence, Integer> identifiers = new HashMap<>();
    private int ids = 0;

    private void getTokens(VirtualFile file, List<PToken> tokens) throws IOException {
        var lexer = new JavaLexer(LanguageLevel.HIGHEST);
        lexer.start(Files.readString(file.toNioPath()));
        while (lexer.getTokenType() != null) {
            short idx = lexer.getTokenType().getIndex();
            if (idx == 1) {
                CharSequence id = lexer.getTokenSequence();
                int i = identifiers.computeIfAbsent(id, cs -> ids++);
                tokens.add(new PToken(i, true, lexer.getTokenStart(), lexer.getTokenEnd(), file));
            } else if (idx != 256) {
                tokens.add(new PToken(idx, false, lexer.getTokenStart(), lexer.getTokenEnd(), file));
            }
            lexer.advance();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var lang = Language.findInstance(JavaLanguage.class);
        if (lang == null) {
            return;
        }
        var javaFileType = lang.getAssociatedFileType();

        var project = event.getProject();
        if (project == null) {
            return;
        }

        var javaFiles = new ArrayList<VirtualFile>();
        ProjectFileIndex.getInstance(project).iterateContent(virtualFile -> {
            if (virtualFile.getFileType() == javaFileType) {
                javaFiles.add(virtualFile);
            }

            return true;
        });

        var tokens = new ArrayList<PToken>();
        for (int i = 0; i < javaFiles.size(); i++) {
            for (int j = i + 1; j < javaFiles.size(); j++) {
                try {
                    var firstFile = javaFiles.get(i);
                    var secondFile = javaFiles.get(j);
                    getTokens(firstFile, tokens);
                    getTokens(secondFile, tokens);
                    tokens.add(new PToken(Integer.MIN_VALUE, false, -1, -1, null));

                    var dups = new ArrayList<Dup>();
                    var t = new Pdup<>(tokens, ids, len -> (p1, p2) -> {
                        try {
                            var t1 = tokens.get(p1);
                            var t2 = tokens.get(p1 + len - 1);

                            VirtualFile file1;
                            if (t1.file.equals(firstFile)) {
                                file1 = firstFile;
                            } else {
                                file1 = secondFile;
                            }
                            var doc = FileDocumentManager.getInstance()
                                    .getDocument(file1);
                            var code1 = doc.getText(new TextRange(t1.start, t2.stop));

                            t1 = tokens.get(p2);
                            t2 = tokens.get(p2 + len - 1);
                            VirtualFile file2;
                            if (t1.file.equals(firstFile)) {
                                file2 = firstFile;
                            } else {
                                file2 = secondFile;
                            }
                            doc = FileDocumentManager.getInstance()
                                    .getDocument(file2);
                            var code2 = doc.getText(new TextRange(t1.start, t2.stop));

                            dups.add(new Dup(file1, code1, file2, code2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    t.build();
                    t.pdup();

                    var tool = ToolWindowManager.getInstance(project).registerToolWindow("Code Duplication", b -> {
                        // TODO: move logic inside tool window factory to allow for updates
                        b.contentFactory = new PdupToolWindowFactory(project, dups);
                        b.anchor = ToolWindowAnchor.BOTTOM;
                        b.canCloseContent = true;
                        return Unit.INSTANCE;
                    });
                    tool.setAutoHide(true);
                    tool.show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
