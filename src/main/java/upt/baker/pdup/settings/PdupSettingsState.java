package upt.baker.pdup.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "upt.baker.pdup.settings.SettingsState",
        storages = @Storage("PdupSettings.xml")
)
public class PdupSettingsState implements PersistentStateComponent<PdupSettingsState> {
    public int tokenLen = 200;
    public int lines = 0;

    public static PdupSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(PdupSettingsState.class);
    }

    @Override
    public @Nullable PdupSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PdupSettingsState pdupSettingsState) {
        XmlSerializerUtil.copyBean(pdupSettingsState, this);
    }
}
