package upt.baker.pdup;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DupManager {
    private final Map<CharSequence, Integer> identifiers = new HashMap<>();
    private int ids = 0;

    private final Project project;

    public DupManager(Project project) {
        this.project = project;
    }

    private void getTokens(VirtualFile file, List<PToken> tokens) throws IOException {
        var lexer = new JavaLexer(LanguageLevel.HIGHEST);
        var doc = FileDocumentManager.getInstance().getDocument(file);
        if (doc == null) {
            return;
        }
        lexer.start(doc.getText());
        while (lexer.getTokenType() != null) {
            short idx = lexer.getTokenType().getIndex();

            // whitespace || end of line comment || doc comment
            if (idx == 132 || idx == 3 || idx == 544) {
                System.out.print("");
            } else if (idx == 1) {
                CharSequence id = lexer.getTokenSequence();
                int i = identifiers.computeIfAbsent(id, cs -> ids++);
                tokens.add(new PToken(i, true, lexer.getTokenStart(), lexer.getTokenEnd(), file, lexer.getTokenText().trim()));
            } else if (idx != 256) { // TODO: 256 = ?
                tokens.add(new PToken(idx, false, lexer.getTokenStart(), lexer.getTokenEnd(), file, lexer.getTokenText().trim()));
            }
            lexer.advance();
        }
    }

    private @Nullable String getCodeSegment(PToken startToken, PToken endToken) {
        if (!startToken.file.equals(endToken.file)) {
            return null;
        }

        Document doc = FileDocumentManager.getInstance()
                .getDocument(startToken.file);

        if (doc == null) {
            return null;
        }
        var txt = "";
        try {
            txt = doc.getText(new TextRange(startToken.start, endToken.end));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return txt;
    }

    public List<Dup> getDups() {
        var lang = Language.findInstance(JavaLanguage.class);
        if (lang == null) {
            return List.of();
        }
        var javaFileType = lang.getAssociatedFileType();

        var javaFiles = new ArrayList<VirtualFile>();
        ProjectFileIndex.getInstance(project).iterateContent(virtualFile -> {
            if (virtualFile.getFileType() == javaFileType) {
                javaFiles.add(virtualFile);
            }

            return true;
        });

        var dups = new ArrayList<Dup>();
        for (int i = 0; i < javaFiles.size(); i++) {
            for (int j = i + 1; j < javaFiles.size(); j++) {
                var firstFile = javaFiles.get(i);
                var secondFile = javaFiles.get(j);
                if (firstFile.getName().equals("ConvertToClsSet.java") && secondFile.getName().equals("ConvertArscFile.java")) {
                    System.out.println();
                } else {
                    continue;
                }
                try {
                    var tokens = new ArrayList<PToken>();
                    getTokens(firstFile, tokens);
//                    getTokens(secondFile, tokens);
                    tokens.add(new PToken(Integer.MIN_VALUE, false, -1, -1, null, "EOF"));

                    var t = new Pdup<>(tokens, ids, len -> (p1, p2) -> {
                        try {
                            var firstStart = tokens.get(p1);
                            var firstEnd = tokens.get(p1 + len - 1);
                            String firstSegment = getCodeSegment(firstStart, firstEnd);
                            var secondStart = tokens.get(p2);
                            var secondEnd = tokens.get(p2 + len - 1);
                            String secondSegment = getCodeSegment(secondStart, secondEnd);

                            if (firstSegment == null || secondSegment == null) {
                                return;
                            }

                            dups.add(new Dup(firstStart.file, firstSegment, firstStart.start, firstEnd.end,
                                    secondStart.file, secondSegment, secondStart.start, secondEnd.end));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    t.build();
                    t.pdup();


                } catch (Exception e) {
                    e.printStackTrace();
                    return dups;
                }
            }
        }

        return dups;
    }
}
