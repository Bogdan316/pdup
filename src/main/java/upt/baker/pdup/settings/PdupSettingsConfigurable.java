package upt.baker.pdup.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nullable;
import upt.baker.pdup.index.PdupFileIndex;

import javax.swing.*;
import java.util.Arrays;

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
                || settingsComponent.getLines() != settings.lines
                || !Arrays.equals(settingsComponent.getPatterns(), settings.patterns);
    }

    @Override
    public void apply() {
        var settings = PdupSettingsState.getInstance();
        settings.tokenLen = settingsComponent.getTokenLen();
        settings.lines = settingsComponent.getLines();
        var p = !Arrays.equals(settingsComponent.getPatterns(), settings.patterns);
        settings.patterns = settingsComponent.getPatterns();
        if (p) {
            FileBasedIndex.getInstance()
                    .scheduleRebuild(PdupFileIndex.NAME, new RuntimeException("Error while rebuilding Pdup index"));
        }
    }

    @Override
    public void reset() {
        var settings = PdupSettingsState.getInstance();
        settingsComponent.setTokenLen(settings.tokenLen);
        settingsComponent.setLines(settings.lines);
        settingsComponent.setPatterns(settings.patterns);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
