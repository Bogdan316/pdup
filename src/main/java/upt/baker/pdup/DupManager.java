package upt.baker.pdup;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileBasedIndex;
import upt.baker.pdup.index.PdupFileIndex;
import upt.baker.pdup.settings.PdupSettingsState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        var docManager = FileDocumentManager.getInstance();
        var docCache = new HashMap<VirtualFile, Document>();

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
                    theTokens.add(new PdupToken(Integer.MIN_VALUE, -1, -1));

                    // TODO: make tree clonable, keep the tree from the first file
                    // TODO: this introduces duplicates
                    // TODO: add listener to update index
                    var t = new Pdup<>(state.tokenLen, theTokens, len -> (p1, p2) -> {
                        try {
                            int end = p1 + len - 1;
                            if (!(p1 < tokMid && end < tokMid || p1 >= tokMid && end >= tokMid)) {
                                return;
                            }
                            var fstFile = p1 < tokMid ? secondFile : firstFile;
                            var firstRange = new TextRange(theTokens.get(p1).startOffset, theTokens.get(end).endOffset);

                            end = p2 + len - 1;
                            if (!(p2 < tokMid && end < tokMid || p2 >= tokMid && end >= tokMid)) {
                                return;
                            }
                            var sndFile = p2 < tokMid ? secondFile : firstFile;
                            var secondRange = new TextRange(theTokens.get(p2).startOffset, theTokens.get(end).endOffset);

                            var firstDoc = docCache.computeIfAbsent(fstFile, docManager::getDocument);
                            var secondDoc = docCache.computeIfAbsent(sndFile, docManager::getDocument);

                            var d = new Dup(firstDoc, firstRange, secondDoc, secondRange);
                            if (d.firstSegLines() >= state.lines || d.secondSegLines() >= state.lines) {
                                dups.add(d);
                            }
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
            break;
        }

        System.out.println("DONE");
        return dups;
    }
}
