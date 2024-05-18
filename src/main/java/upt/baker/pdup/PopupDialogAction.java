package upt.baker.pdup;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

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
