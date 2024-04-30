package upt.baker.pdup.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PdupSettingsConfigurable implements Configurable {
    private PdupSettingsComponent settingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Pdup Settings";
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new PdupSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        var settings = PdupSettingsState.getInstance();
        return settingsComponent.getTokenLen() != settings.tokenLen
                || settingsComponent.getLines() != settings.lines;
    }

    @Override
    public void apply() {
        var settings = PdupSettingsState.getInstance();
        settings.tokenLen = settingsComponent.getTokenLen();
        settings.lines = settingsComponent.getLines();
    }

    @Override
    public void reset() {
        var settings = PdupSettingsState.getInstance();
        settingsComponent.setTokenLen(settings.tokenLen);
        settingsComponent.setLines(settings.lines);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
