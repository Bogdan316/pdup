package upt.baker.pdup.settings;

import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

public class PdupSettingsComponent {
    private final JPanel mainPanel;
    private final JBIntSpinner tokenLenSpinner = new JBIntSpinner(200, 5, Integer.MAX_VALUE);

    public PdupSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Minimum token length"), tokenLenSpinner, 1, false)
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
}
