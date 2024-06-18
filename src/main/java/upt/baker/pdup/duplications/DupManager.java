package upt.baker.pdup.duplications;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nullable;
import upt.baker.pdup.index.PdupFileIndex;
import upt.baker.pdup.settings.PdupSettingsState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DupManager {
    private final Project project;
    private final PdupSettingsState state;
    private final FileBasedIndex fileBasedIdx;
    private final ProjectFileIndex projFileIdx;
    private final Map<VirtualFile, Document> docCache = new HashMap<>();
    private final FileDocumentManager docManager;

    public DupManager(Project project) {
        this.project = project;
        this.state = PdupSettingsState.getInstance().getState();
        this.fileBasedIdx = FileBasedIndex.getInstance();
        this.projFileIdx = ProjectFileIndex.getInstance(project);
        this.docManager = FileDocumentManager.getInstance();
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

    private @Nullable Dup convertDupRange(DupRange range, VirtualFile lftFile, VirtualFile rhtFile, int tokMid, List<PdupToken> tokens) {
        try {
            var lFile = range.isLftBefore(tokMid) ? rhtFile : lftFile;
            var lRange = new TextRange(tokens.get(range.lftStartOff()).startOffset, tokens.get(range.lftEndOff()).endOffset);

            var rFile = range.isBefore(tokMid) ? rhtFile : lftFile;
            var rRange = new TextRange(tokens.get(range.rhtStartOff()).startOffset, tokens.get(range.rhtEndOff()).endOffset);

            var lftDoc = docCache.computeIfAbsent(lFile, docManager::getDocument);
            var rhtDoc = docCache.computeIfAbsent(rFile, docManager::getDocument);

            var d = new Dup(lftDoc, lRange, rhtDoc, rRange);
            if (d.firstSegLines() >= state.lines || d.secondSegLines() >= state.lines) {
                return d;
            }
        } catch (Exception e) {
            // TODO: add logging
        }

        return null;
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


        // TODO: make tree clonable, keep the tree from the first file
        // TODO: this introduces duplicates ?
        // TODO: add listener to update index
        // TODO: treat literals the same

        var dups = new ArrayList<Dup>();
        int size = javaFiles.size();
        for (int i = 0; i < size; i++) {
            var firstFile = javaFiles.get(i);
            var tokens = getTokens(firstFile);
            int off = tokens.stream().mapToInt(PdupToken::getIdx).max().orElse(0);
            for (int j = i + 1; j < size; j++) {
                var secondFile = javaFiles.get(j);
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

                var t = new Pdup<>(state.tokenLen, theTokens);
                var ranges = t.pdup();
                if (ranges.isEmpty()) {
                    continue;
                }
                for (var r : ranges) {
                    if (!r.isBefore(tokMid) && !r.isAfter(tokMid)) {
                        var dup = convertDupRange(r, firstFile, secondFile, tokMid, theTokens);
                        if (dup != null) {
                            dups.add(dup);
                        }
                    }
                }

                System.out.println(dups.size());
            }

        }

        for (int i = 0; i < size; i++) {
            var file = javaFiles.get(i);
            if(file.toString().contains("SmaliDebugger.java")){
                System.out.println();
            }
            var tokens = getTokens(file);
            tokens.add(new PdupToken(Integer.MIN_VALUE, -1, -1));

            var t = new Pdup<>(state.tokenLen, tokens);
            var ranges = t.pdup();
            if(ranges.isEmpty()){
                continue;
            }
            var it = ranges.iterator();
            var s = it.next();
            while (it.hasNext()) {
                var d = it.next();
                if (s.overlaps(d)) {
                    s = s.add(d);
                } else {
                    var dup = convertDupRange(s, file, file, tokens.size(), tokens);
                    if (dup != null) {
                        dups.add(dup);
                    }
                    s = d;
                }
            }
            var dup = convertDupRange(s, file, file, tokens.size(), tokens);
            if (dup != null) {
                dups.add(dup);
            }
        }

        System.out.println("DONE");
        return dups;
    }
}
