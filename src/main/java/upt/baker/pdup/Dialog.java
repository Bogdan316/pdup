package upt.baker.pdup;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffManagerEx;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.java.refactoring.JavaRefactoringBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class Dialog extends DialogWrapper {
    private static final @NonNls String DIFF_PLACE = "ExtractSignature";

    private final VirtualFile leftFile;
    private final String leftContent;
    private final VirtualFile rightFile;
    private final String rightContent;
    private final Project project;

    public Dialog(Project project, VirtualFile leftFile, String leftContent, VirtualFile rightFile, String rightContent) {
        super(project);
        this.project = project;
        this.leftFile = leftFile;
        this.leftContent = leftContent;
        this.rightFile = rightFile;
        this.rightContent = rightContent;
        setTitle(JavaRefactoringBundle.message("extract.parameters.to.replace.duplicates"));
        setOKButtonText(JavaRefactoringBundle.message("accept.signature.change"));
        setCancelButtonText(JavaRefactoringBundle.message("keep.original.signature"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createNorthPanel() {
        return new JLabel("Test dialog");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        DiffContentFactory contentFactory = DiffContentFactory.getInstance();
        DocumentContent oldContent = contentFactory.create(leftContent, leftFile);
        DocumentContent newContent = contentFactory.create(rightContent, rightFile);
        SimpleDiffRequest request = new SimpleDiffRequest(null, oldContent, newContent, "test1", "test2");

        DiffRequestPanel diffPanel = DiffManager.getInstance().createRequestPanel(project, getDisposable(), null);
        diffPanel.putContextHints(DiffUserDataKeys.PLACE, DIFF_PLACE);
        diffPanel.setRequest(request);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(diffPanel.getComponent(), BorderLayout.CENTER);
        panel.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.insetsTop(5)));
        return panel;
    }
}