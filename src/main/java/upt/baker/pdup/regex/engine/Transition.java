package upt.baker.pdup.regex.engine;

import org.jetbrains.annotations.Nullable;

public class Transition {
    public @Nullable State dst;

    public void setDst(@Nullable State dst) {
        this.dst = dst;
    }
}
