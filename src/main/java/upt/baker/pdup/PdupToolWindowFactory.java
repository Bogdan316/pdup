package upt.baker.pdup;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.ide.SelectInEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.OnePixelSplitter;
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
    private final List<Dup> dups;
    private final Project project;

    public PdupToolWindowFactory(Project project, List<Dup> dups) {
        this.project = project;
        this.dups = dups;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PdupToolWindowContent toolWindowContent = new PdupToolWindowContent(toolWindow);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private class PdupToolWindowContent {
        private final OnePixelSplitter contentPanel = new OnePixelSplitter();
        private final ToolWindow toolWindow;

        public PdupToolWindowContent(ToolWindow toolWindow) {
            this.toolWindow = toolWindow;
            contentPanel.setLayout(new BorderLayout(0, 20));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
            createContentPanel();
        }

        private void createContentPanel() {
            contentPanel.setFirstComponent(getLeftPane());
            contentPanel.setSecondComponent(getDiff(dups.get(0)));
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
                    return VirtualFile.class;
                }
            };
            model.addColumn("a");
            model.addColumn("b");
            for (var d : dups) {
                model.addRow(new Object[]{d.firstFile(), d.secondFile()});
            }

            var table = new JBTable(model);
            var selModel = table.getSelectionModel();
            table.setDefaultRenderer(VirtualFile.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    return super.getTableCellRendererComponent(table, ((VirtualFile) value).getName(), isSelected, hasFocus, row, column);
                }
            });
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        var source = (JBTable) e.getSource();
                        int row = source.getSelectedRow();
                        int col = source.getSelectedColumn();
                        var file = (VirtualFile) table.getValueAt(row, col);
                        SelectInEditorManager.getInstance(project).selectInEditor(
                                file,
                                0,
                                10,
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
            return table;
        }

        private JComponent getDiff(Dup dup) {
            DiffContentFactory contentFactory = DiffContentFactory.getInstance();
            DocumentContent oldContent = contentFactory.create(dup.firstCode(), dup.firstFile());
            DocumentContent newContent = contentFactory.create(dup.secondCode(), dup.secondFile());
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
