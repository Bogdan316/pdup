package upt.baker.pdup;

import com.intellij.execution.process.mediator.daemon.ProcessManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.progress.util.ReadTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import upt.baker.pdup.duplications.Dup;
import upt.baker.pdup.duplications.DupManager;

import java.util.ArrayList;

public class PopupDialogAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        var manager = ToolWindowManager.getInstance(project);
        var tool = manager.getToolWindow("Code Duplication");
        if (tool == null) {
            tool = manager.registerToolWindow("Code Duplication", b -> {
                b.contentFactory = new PdupToolWindowFactory();
                b.anchor = ToolWindowAnchor.BOTTOM;
                b.canCloseContent = true;
                return Unit.INSTANCE;
            });
        } else {
            tool.getComponent().add(new PdupToolWindowFactory.PdupToolWindowContent(project).getContentPanel());
        }

        tool.setAutoHide(true);
        tool.show();
    }
}
