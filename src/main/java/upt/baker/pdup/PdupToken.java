package upt.baker.pdup;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class PdupToken {
    public final int idx;

    public final int offset;

    public PdupToken(int idx, int offset) {
        this.idx = idx;
        this.offset = offset;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(idx);
        dataOutput.writeInt(offset);
    }

    public static PdupToken read(DataInput dataInput) throws IOException {
        return new PdupToken(dataInput.readInt(), dataInput.readInt());
    }

    @Override
    public String toString() {
        return "PdupToken{" +
                "idx=" + idx +
                ", offset=" + offset +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PdupToken pdupToken = (PdupToken) o;
        return idx == pdupToken.idx && offset == pdupToken.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx, offset);
    }
}
