package upt.baker.pdup;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

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

    private List<PToken> getTokens(VirtualFile file) {
        var tokens = new ArrayList<PToken>();
        var lexer = new JavaLexer(LanguageLevel.HIGHEST);
        var doc = FileDocumentManager.getInstance().getDocument(file);
        if (doc == null) {
            return List.of();
        }
        lexer.start(doc.getText());
        while (lexer.getTokenType() != null) {
            short idx = lexer.getTokenType().getIndex();

            // whitespace || end of line comment || doc comment
            if (idx == 132 || idx == 3 || idx == 544) {
                lexer.advance();
                continue;
            } else if (idx == 1) {
                CharSequence id = lexer.getTokenSequence();
                int i = identifiers.computeIfAbsent(id, cs -> ids++);
                tokens.add(new PToken(i, true, lexer.getTokenStart(), lexer.getTokenEnd(), file, lexer.getTokenText().trim()));
            } else if (idx != 256) { // TODO: 256 = ?
                tokens.add(new PToken(idx, false, lexer.getTokenStart(), lexer.getTokenEnd(), file, lexer.getTokenText().trim()));
            }
            lexer.advance();
        }

        return tokens;
    }

    private boolean isCodeSegmentInvalid(PToken startToken, PToken endToken) {
        if (!startToken.file.equals(endToken.file)) {
            return true;
        }

        Document doc = FileDocumentManager.getInstance()
                .getDocument(startToken.file);

        return doc == null;
    }

    public List<Dup> getDups() {
        var lang = Language.findInstance(JavaLanguage.class);
        if (lang == null) {
            return List.of();
        }
        var javaFileType = lang.getAssociatedFileType();

        var javaFiles = new ArrayList<VirtualFile>();
        var index = ProjectFileIndex.getInstance(project);
        index.iterateContent(virtualFile -> {
            if (virtualFile.getFileType() == javaFileType && !index.isUnderSourceRootOfType(virtualFile, JavaModuleSourceRootTypes.TESTS)) {
                javaFiles.add(virtualFile);
            }

            return true;
        });

        var dups = new ArrayList<Dup>();
        int start = 0;
        int size = javaFiles.size();
        for (int i = start; i < size; i++) {
            var firstFile = javaFiles.get(i);
            var tokens = getTokens(firstFile);
            for (int j = i + 1; j < size; j++) {
                var secondFile = javaFiles.get(j);
                try {
                    var theTokens = getTokens(secondFile);
                    theTokens.addAll(tokens);
                    theTokens.add(new PToken(Integer.MIN_VALUE, false, -1, -1, null, "EOF"));

                    var t = new Pdup<>(theTokens, ids, len -> (p1, p2) -> {
                        try {
                            var firstStart = theTokens.get(p1);
                            var firstEnd = theTokens.get(p1 + len - 1);
                            if (isCodeSegmentInvalid(firstStart, firstEnd)) {
                                return;
                            }

                            var secondStart = theTokens.get(p2);
                            var secondEnd = theTokens.get(p2 + len - 1);
                            if (isCodeSegmentInvalid(secondStart, secondEnd)) {
                                return;
                            }

                            dups.add(new Dup(firstStart.file, firstStart.start, firstEnd.end,
                                    secondStart.file, secondStart.start, secondEnd.end));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    t.build();
                    t.pdup();
                    System.out.println(dups.size());

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(firstFile + " -> " + secondFile);
                    return dups;
                }
            }
        }

        System.out.println("DONE");
        System.exit(0)/**/;
        return dups;
    }
}
