package upt.baker.pdup.index;

import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;
import upt.baker.pdup.duplications.PdupToken;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdupDataExternalizer implements DataExternalizer<List<PdupToken>> {
    @Override
    public void save(@NotNull DataOutput dataOutput, List<PdupToken> tokens) throws IOException {
        dataOutput.writeInt(tokens.size());
        for (var t : tokens) {
            t.write(dataOutput);
        }
    }

    @Override
    public List<PdupToken> read(@NotNull DataInput dataInput) throws IOException {
        int size = dataInput.readInt();
        var out = new ArrayList<PdupToken>(size);
        for (int i = 0; i < size; i++) {
            out.add(PdupToken.read(dataInput));
        }

        return out;
    }
}
