package upt.baker.pdup;

import com.intellij.ide.SelectInEditorManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import upt.baker.pdup.inlay.IdentifierElementRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PdupToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PdupToolWindowContent toolWindowContent = new PdupToolWindowContent(project);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static class PdupToolWindowContent {
        private final OnePixelSplitter contentPanel = new OnePixelSplitter();
        private final Project project;
        private final List<Dup> dups;

        public PdupToolWindowContent(Project project) {
            this.project = project;
            contentPanel.setLayout(new BorderLayout(0, 20));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
            // TODO: this way the panel will not update, it is created only once
            this.dups = new DupManager(project).getDups();
            createContentPanel();
        }

        private void createContentPanel() {
            contentPanel.setFirstComponent(getLeftPane());
            if (!dups.isEmpty()) {
                contentPanel.setSecondComponent(getDiff(dups.get(0)));
            }
            contentPanel.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.insetsTop(5)));
            contentPanel.setProportion(0.20f);
        }

        private JComponent getLeftPane() {
            var model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return Dup.class;
                }
            };
            model.addColumn("");
            model.addColumn("");
            for (var d : dups) {
                model.addRow(new Object[]{d, d});
            }

            var table = new JBTable(model);
            var selModel = table.getSelectionModel();
            table.setDefaultRenderer(Dup.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    var dup = (Dup) value;
                    String fileName = dup.firstFile().getName();
                    if (column == 1) {
                        fileName = dup.secondFile().getName();
                    }
                    return super.getTableCellRendererComponent(table, fileName, isSelected, hasFocus, row, column);
                }
            });
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        var source = (JBTable) e.getSource();
                        int row = source.getSelectedRow();
                        int col = source.getSelectedColumn();
                        var dup = (Dup) table.getValueAt(row, col);
                        VirtualFile file = dup.firstFile();
                        var range = dup.firstRange();
                        if (col == 1) {
                            file = dup.secondFile();
                            range = dup.secondRange();
                        }
                        SelectInEditorManager.getInstance(project).selectInEditor(
                                file,
                                range.getStartOffset(),
                                range.getEndOffset(),
                                false,
                                false
                        );
                    }
                }
            });

            selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selModel.addListSelectionListener(e -> {
                var lsm = (ListSelectionModel) e.getSource();
                contentPanel.setSecondComponent(getDiff(dups.get(lsm.getMinSelectionIndex())));
            });
            table.setRowSelectionInterval(0, 0);
            return new JBScrollPane(table);
        }

        private JComponent getDiff(Dup dup) {
            var factory = EditorFactory.getInstance();
            var lftDoc = factory.createDocument(dup.getFirstCodeSegment());
            var lftEditor = factory.createEditor(lftDoc, project, dup.firstFile(), true);

            var rhtDoc = factory.createDocument(dup.getSecondCodeSegment());
            var rhtEditor = factory.createEditor(rhtDoc, project, dup.firstFile(), true);
            var panel = new OnePixelSplitter();
            panel.setFirstComponent(lftEditor.getComponent());
            panel.setSecondComponent(rhtEditor.getComponent());

            // sync scroll bars
            lftEditor.getScrollingModel().addVisibleAreaListener(visibleAreaEvent -> {
                int off = visibleAreaEvent.getEditor().getScrollingModel().getVerticalScrollOffset();
                rhtEditor.getScrollingModel().scrollVertically(off);
            });

            rhtEditor.getScrollingModel().addVisibleAreaListener(visibleAreaEvent -> {
                int off = visibleAreaEvent.getEditor().getScrollingModel().getVerticalScrollOffset();
                lftEditor.getScrollingModel().scrollVertically(off);
            });

            var psiFactory = PsiFileFactory.getInstance(project);
            var lftFile = psiFactory.createFileFromText(JavaFileType.INSTANCE.getLanguage(), dup.getFirstCodeSegment());
            var lftIds = PsiTreeUtil.collectElementsOfType(lftFile, PsiIdentifier.class).iterator();

            var rhtFile = psiFactory.createFileFromText(JavaFileType.INSTANCE.getLanguage(), dup.getSecondCodeSegment());
            var rhtIds = PsiTreeUtil.collectElementsOfType(rhtFile, PsiIdentifier.class).iterator();

            var lftInlay = lftEditor.getInlayModel();
            var rhtInlay = rhtEditor.getInlayModel();

            while (lftIds.hasNext() && rhtIds.hasNext()) {
                var li = lftIds.next();
                var ri = rhtIds.next();
                if (!li.getText().equals(ri.getText())) {
                    lftInlay.addInlineElement(li.getTextRange().getEndOffset(), new IdentifierElementRenderer(":" + ri.getText()));
                    rhtInlay.addInlineElement(ri.getTextRange().getStartOffset(), new IdentifierElementRenderer(li.getText() + ":"));
                }
            }
            return panel;
        }


        public JPanel getContentPanel() {
            return contentPanel;
        }
    }
}
