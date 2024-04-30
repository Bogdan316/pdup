package upt.baker.pdup.index;

import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.JavaDocElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.SingleEntryIndexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import upt.baker.pdup.PdupToken;

import java.util.*;

public class PdupDataIndexer extends SingleEntryIndexer<List<PdupToken>> {
    private static final TokenSet IGNORED = TokenSet.orSet(TokenSet.create(
            TokenType.WHITE_SPACE, JavaTokenType.END_OF_LINE_COMMENT, JavaTokenType.C_STYLE_COMMENT
    ), JavaDocElementType.ALL_JAVADOC_ELEMENTS);

    protected PdupDataIndexer(boolean acceptNullValues) {
        super(acceptNullValues);
    }

    private List<PdupToken> depthFirst(PsiFile element) {
        var identifiers = new HashMap<String, Integer>();
        int id = 0;

        var stack = new ArrayDeque<PsiElement>();
        stack.push(element);
        var tokens = new ArrayList<PdupToken>();
        var elements = new ArrayList<PsiElement>();
        while (!stack.isEmpty()) {
            var e = stack.pop();
            if (IGNORED.contains(e.getNode().getElementType())) {
                continue;
            }

            if (PsiUtil.isJavaToken(e, JavaTokenType.IDENTIFIER)) {
                var key = e.getText().trim();
                var next = identifiers.get(key);
                if (next == null) {
                    identifiers.put(key, id);
                    next = id;
                    id++;
                }
                tokens.add(new PdupToken(next, e.getTextOffset()));
                elements.add(e);
            } else if (e instanceof PsiJavaToken token) {
                tokens.add(new PdupToken(-token.getTokenType().getIndex(), e.getTextOffset()));
                elements.add(e);
            } else {
                var ch = e.getChildren();
                for (int i = ch.length - 1; i >= 0; i--) {
                    stack.push(ch[i]);
                }
            }
        }

        return tokens;
    }

    private boolean isSourceFile(FileContent fileContent) {
        var file = fileContent.getPsiFile().getVirtualFile();
        var project = fileContent.getProject();
        var index = ProjectFileIndex.getInstance(project);
        return index.isInSourceContent(file)
                && !index.isInTestSourceContent(file)
                && !index.isInLibrary(file);
    }


    @Override
    protected @Nullable List<PdupToken> computeValue(@NotNull FileContent fileContent) {
        if (!isSourceFile(fileContent)) {
            return List.of();
        }
        var psiFile = fileContent.getPsiFile();

        return depthFirst(psiFile);
    }
}
