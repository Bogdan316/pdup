package upt.baker.pdup;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.source.tree.JavaDocElementType;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.indexing.FileBasedIndex;
import upt.baker.pdup.index.PdupFileIndex;
import upt.baker.pdup.settings.PdupSettingsState;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DupManager {
    private final Project project;
    private final PdupSettingsState state;

    public DupManager(Project project) {
        this.project = project;
        this.state = PdupSettingsState.getInstance().getState();
    }

    public List<Dup> getDups() {
        var fileIndex = FileBasedIndex.getInstance();
        var lang = Language.findInstance(JavaLanguage.class);
        if (lang == null) {
            return List.of();
        }

        var javaFiles = new ArrayList<VirtualFile>();
        var index = ProjectFileIndex.getInstance(project);
        index.iterateContent(virtualFile -> {
            if (virtualFile.getFileType().getName().equals("JAVA") && index.isInSourceContent(virtualFile)
                    && !index.isInTestSourceContent(virtualFile) && !index.isInLibrary(virtualFile)) {
                javaFiles.add(virtualFile);
            }
            return true;
        });

        var dups = new ArrayList<Dup>();
        int size = javaFiles.size();
        for (int i = 0; i < size; i++) {
            var firstFile = javaFiles.get(i);
            var tokens = fileIndex.getSingleEntryIndexData(PdupFileIndex.NAME, firstFile, project);
            // TODO: rebuild when null
            if (tokens == null) {
                continue;
            }
            for (int j = i + 1; j < size; j++) {
                var secondFile = javaFiles.get(j);
                try {
                    var theTokens = fileIndex.getSingleEntryIndexData(PdupFileIndex.NAME, secondFile, project);
                    if (theTokens == null) {
                        continue;
                    }
                    int tokMid = theTokens.size();
                    theTokens.addAll(tokens);
                    theTokens.add(new PdupToken(Integer.MIN_VALUE, -1));

                    var t = new Pdup<>(state.tokenLen, theTokens, len -> (p1, p2) -> {
                        try {
                            int end = p1 + len - 1;
                            if (!(p1 < tokMid && end < tokMid || p1 >= tokMid && end >= tokMid)) {
                                return;
                            }
                            var fstFile = p1 < tokMid ? secondFile : firstFile;
                            var firstStart = theTokens.get(p1);
                            var firstEnd = theTokens.get(end);

                            end = p2 + len - 1;
                            if (!(p2 < tokMid && end < tokMid || p2 >= tokMid && end >= tokMid)) {
                                return;
                            }
                            var sndFile = p2 < tokMid ? secondFile : firstFile;
                            var secondStart = theTokens.get(p2);
                            var secondEnd = theTokens.get(p2 + len - 1);

                            dups.add(new Dup(PsiUtilCore.getPsiFile(project, fstFile), firstStart.offset, firstEnd.offset,
                                    PsiUtilCore.getPsiFile(project, sndFile), secondStart.offset, secondEnd.offset));
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
        return dups;
    }
}
