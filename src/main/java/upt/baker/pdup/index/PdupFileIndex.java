package upt.baker.pdup.index;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;
import upt.baker.pdup.PdupToken;

import java.util.List;

public class PdupFileIndex extends SingleEntryFileBasedIndexExtension<List<PdupToken>> {
    public static final ID<Integer, List<PdupToken>> NAME = ID.create(PdupFileIndex.class.getName());

    @Override
    public @NotNull ID<Integer, List<PdupToken>> getName() {
        return NAME;
    }

    @Override
    public @NotNull SingleEntryIndexer<List<PdupToken>> getIndexer() {
        return new PdupDataIndexer(false);
    }

    @Override
    public @NotNull DataExternalizer<List<PdupToken>> getValueExternalizer() {
        return new PdupDataExternalizer();
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType().equals(JavaFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
