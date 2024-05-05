package upt.baker.pdup;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileManagerListener;
import com.intellij.util.indexing.FileBasedIndex;
import upt.baker.pdup.index.PdupFileIndex;
import upt.baker.pdup.settings.PdupSettingsState;

import java.util.*;

public class DupManager {
    private final Project project;
    private final PdupSettingsState state;
    private final FileBasedIndex fileBasedIdx;
    private final ProjectFileIndex projFileIdx;

    public DupManager(Project project) {
        this.project = project;
        this.state = PdupSettingsState.getInstance().getState();
        this.fileBasedIdx = FileBasedIndex.getInstance();
        this.projFileIdx = ProjectFileIndex.getInstance(project);
    }

    private List<PdupToken> getTokens(VirtualFile file) {
        var tokens = fileBasedIdx.getSingleEntryIndexData(PdupFileIndex.NAME, file, project);
        if (tokens == null) {
            fileBasedIdx.requestReindex(file);
            tokens = fileBasedIdx.getSingleEntryIndexData(PdupFileIndex.NAME, file, project);
        }

        if (tokens == null) {
            return new ArrayList<>();
        }

        return tokens;
    }


    public List<Dup> getDups() {
        var lang = Language.findInstance(JavaLanguage.class);
        if (lang == null) {
            return List.of();
        }

        var javaFiles = new ArrayList<VirtualFile>();
        projFileIdx.iterateContent(virtualFile -> {
            if (virtualFile.getFileType().getName().equals("JAVA") && projFileIdx.isInSourceContent(virtualFile)
                    && !projFileIdx.isInTestSourceContent(virtualFile) && !projFileIdx.isInLibrary(virtualFile)) {
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
            var tokens = getTokens(firstFile);
            int off = tokens.stream().mapToInt(PdupToken::getIdx).max().orElse(0);
            for (int j = i + 1; j < size; j++) {
                var secondFile = javaFiles.get(j);
                try {
                    var theTokens = new ArrayList<>(getTokens(secondFile));
                    if (off >= 0) {
                        // add 1 in case the biggest identifier idx is 0
                        off++;
                        for (var t : theTokens) {
                            if (t.idx >= 0) {
                                t.idx += off;
                            }
                        }
                    }

                    int tokMid = theTokens.size();
                    theTokens.addAll(tokens);
                    theTokens.add(new PdupToken(Integer.MIN_VALUE, -1, -1));

                    // TODO: make tree clonable, keep the tree from the first file
                    // TODO: this introduces duplicates ?
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
