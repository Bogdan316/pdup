package upt.baker.pdup.duplications;

import upt.baker.pdup.utils.KeywordMapping;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class PdupToken {
    public int idx;

    public final int startOffset;
    public final int endOffset;

    public PdupToken(int idx, int startOffset, int endOffset) {
        this.idx = idx;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public int getIdx() {
        return idx;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(idx);
        dataOutput.writeInt(startOffset);
        dataOutput.writeInt(endOffset);
    }

    public static PdupToken read(DataInput dataInput) throws IOException {
        return new PdupToken(dataInput.readInt(), dataInput.readInt(), dataInput.readInt());
    }

    @Override
    public String toString() {
        return "PdupToken{" +
                "idx=" + idx +
                ", startOffset=" + startOffset +
                ", endOffset=" + endOffset +
                ", debugName=" + KeywordMapping.getName(idx) +
                '}';
    }

    //    @Override
//    public String toString() {
//        return (idx >= 0 ? String.format("%-3s", "p") : String.format("%-3d", idx))+ "|";
//    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PdupToken pdupToken = (PdupToken) o;
        return idx == pdupToken.idx && startOffset == pdupToken.startOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx, startOffset);
    }
}
