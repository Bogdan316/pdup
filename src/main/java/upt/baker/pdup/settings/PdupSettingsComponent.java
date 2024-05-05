package upt.baker.pdup.settings;

import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.ListUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.editors.JBComboBoxTableCellEditorComponent;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

public class PdupSettingsComponent {
    private final JPanel mainPanel;
    private final JBIntSpinner tokenLenSpinner = new JBIntSpinner(200, 5, Integer.MAX_VALUE);
    private final JBIntSpinner linesSpinner = new JBIntSpinner(0, 0, Integer.MAX_VALUE);
    private final ExpandableTextField patterns = new ExpandableTextField(ParametersListUtil.COLON_LINE_PARSER, ParametersListUtil.COLON_LINE_JOINER);

    public PdupSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Minimum token number"), tokenLenSpinner, 1, false)
                .addLabeledComponent(new JBLabel("Minimum line number"), linesSpinner, 1, false)
                .addLabeledComponent(new JBLabel("Excluded patterns"), patterns, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return tokenLenSpinner;
    }

    public int getTokenLen() {
        return tokenLenSpinner.getNumber();
    }

    public void setTokenLen(int number) {
        tokenLenSpinner.setNumber(number);
    }

    public int getLines() {
        return linesSpinner.getNumber();
    }

    public void setLines(int number) {
        linesSpinner.setNumber(number);
    }

    public String[] getPatterns() {
        return patterns.getText().split(";");
    }

    public void setPatterns(String[] patterns) {
        this.patterns.setText(String.join(";", patterns));
    }
}