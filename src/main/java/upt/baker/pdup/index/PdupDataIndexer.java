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
import upt.baker.pdup.duplications.PdupToken;
import upt.baker.pdup.regex.ReMatcher;
import upt.baker.pdup.settings.PdupSettingsState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PdupDataIndexer extends SingleEntryIndexer<List<PdupToken>> {
    private static final TokenSet IGNORED = TokenSet.orSet(TokenSet.create(
            TokenType.WHITE_SPACE, JavaTokenType.END_OF_LINE_COMMENT, JavaTokenType.C_STYLE_COMMENT
    ), JavaDocElementType.ALL_JAVADOC_ELEMENTS);

    private final PdupSettingsState state;

    protected PdupDataIndexer(boolean acceptNullValues) {
        super(acceptNullValues);
        this.state = PdupSettingsState.getInstance().getState();
    }

    private List<PdupToken> depthFirst(PsiFile element) {
        var identifiers = new HashMap<String, Integer>();
        int id = 0;

        var stack = new ArrayDeque<PsiElement>();
        stack.push(element);
        var tokens = new ArrayList<PdupToken>();
        while (!stack.isEmpty()) {
            var e = stack.pop();
            if (IGNORED.contains(e.getNode().getElementType())) {
                continue;
            }

            if (PsiUtil.isJavaToken(e, JavaTokenType.IDENTIFIER)) {
                var key = e.getText().trim();
                var next = identifiers.get(key);
                if (next == null) {
                    // TODO: try hash not ids
                    identifiers.put(key, id);
                    next = id;
                    id++;
                }
                var r = e.getTextRange();
                tokens.add(new PdupToken(next, r.getStartOffset(), r.getEndOffset()));
            } else if (e instanceof PsiJavaToken token) {
                var r = e.getTextRange();
                tokens.add(new PdupToken(-token.getTokenType().getIndex(), r.getStartOffset(), r.getEndOffset()));
            } else {
                var ch = e.getChildren();
                for (int i = ch.length - 1; i >= 0; i--) {
                    stack.push(ch[i]);
                }
            }
        }

        return filterTokens(tokens);
    }

    private List<PdupToken> filterTokens(List<PdupToken> tokens) {
        var pattern = state.getMergedPatterns();
        if (pattern.isBlank()) {
            return tokens;
        }

        var matcher = new ReMatcher(pattern);
        var occur = matcher.findAll(tokens);
        if (occur.isEmpty()) {
            return tokens;
        }

        var copy = new ArrayList<PdupToken>();
        int i = 0;
        for (var o : occur) {
            copy.addAll(tokens.subList(i, o.start()));
            i = o.start() + o.len();
        }
        copy.addAll(tokens.subList(i, tokens.size()));

        return copy;
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
        // TODO: take a look at lighter ast
        var psiFile = fileContent.getPsiFile();

        return depthFirst(psiFile);
    }
}
