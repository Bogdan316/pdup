package upt.baker.pdup;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.ide.SelectInEditorManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PdupToolWindowFactory implements ToolWindowFactory {
    private final Project project;

    public PdupToolWindowFactory(Project project) {
        this.project = project;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PdupToolWindowContent toolWindowContent = new PdupToolWindowContent(project, toolWindow);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static class PdupToolWindowContent {
        private final OnePixelSplitter contentPanel = new OnePixelSplitter();
        private final ToolWindow toolWindow;
        private final Project project;
        private final List<Dup> dups;

        public PdupToolWindowContent(Project project, ToolWindow toolWindow) {
            this.project = project;
            this.toolWindow = toolWindow;
            contentPanel.setLayout(new BorderLayout(0, 20));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
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
            model.addColumn("a");
            model.addColumn("b");
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
                        VirtualFile file = dup.firstVFile();
                        int start = dup.firstStart();
                        int end = dup.firstEnd();
                        if (col == 1) {
                            file = dup.secondVFile();
                            start = dup.secondStart();
                            end = dup.secondEnd();
                        }
                        SelectInEditorManager.getInstance(project).selectInEditor(
                                file,
                                start,
                                end,
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
            DiffContentFactory contentFactory = DiffContentFactory.getInstance();
            DocumentContent oldContent = contentFactory.create(dup.getFirstCodeSegment(), dup.firstVFile());
            DocumentContent newContent = contentFactory.create(dup.getSecondCodeSegment(), dup.secondVFile());
            SimpleDiffRequest request = new SimpleDiffRequest(null, oldContent, newContent, dup.firstFile().getName(), dup.secondFile().getName());

            DiffRequestPanel diffPanel = DiffManager.getInstance().createRequestPanel(project, toolWindow.getDisposable(), null);
            diffPanel.setRequest(request);
            return diffPanel.getComponent();
        }

        public JPanel getContentPanel() {
            return contentPanel;
        }

    }
}
