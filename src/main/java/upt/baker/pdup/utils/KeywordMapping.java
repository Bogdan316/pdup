package upt.baker.pdup.utils;

import com.intellij.psi.JavaTokenType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class KeywordMapping {
    private static final Field[] fields = JavaTokenType.class.getFields();
    private static final Map<Integer, String> idxToKwMap = new HashMap<>(fields.length);
    private static final Map<String, Integer> kwToIdxMap = new HashMap<>(fields.length);

    static {
        for (var f : JavaTokenType.class.getFields()) {
            try {
                var field = f.get(null);
                int idx = (short) field.getClass().getMethod("getIndex").invoke(field);
                if (!field.equals(JavaTokenType.IDENTIFIER)) {
                    idx = -idx;
                }
                idxToKwMap.put(idx, f.getName());
                kwToIdxMap.put(f.getName(), idx);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // ignored
            }
        }
    }

    private KeywordMapping() {
    }

    public static @Nullable Integer getIndex(String name) {
        return kwToIdxMap.get(name);
    }

    public static @Nullable String getName(int idx) {
        if (idx >= 0) {
            return "IDENTIFIER";
        }
        return idxToKwMap.get(idx);
    }
}
