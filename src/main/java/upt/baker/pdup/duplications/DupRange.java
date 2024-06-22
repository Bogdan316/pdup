package upt.baker.pdup.duplications;

import org.jetbrains.annotations.NotNull;

public record DupRange(int lftStartOff, int rhtStartOff, int len) implements Comparable<DupRange> {
    public int lftEndOff() {
        return lftStartOff + len;
    }

    public int rhtEndOff() {
        return rhtStartOff + len;
    }

    public boolean isLftAfter(int idx) {
        return lftStartOff >= idx && lftEndOff() >= idx;
    }

    public boolean isLftBefore(int idx) {
        return lftStartOff < idx && lftEndOff() < idx;
    }

    public boolean isRhtAfter(int idx) {
        return rhtStartOff >= idx && rhtEndOff() >= idx;
    }

    public boolean isRhtBefore(int idx) {
        return rhtStartOff < idx && rhtEndOff() < idx;
    }

    public boolean isAfter(int idx) {
        return isLftAfter(idx) && isRhtAfter(idx);
    }

    public boolean isBefore(int idx) {
        return isLftBefore(idx) && isRhtBefore(idx);
    }

    public boolean overlaps(DupRange o) {
        return lftEndOff() >= o.lftStartOff && rhtEndOff() >= o.rhtStartOff;
    }

    public DupRange add(DupRange o) {
        int l;
        if (lftEndOff() >= o.lftEndOff()) {
            l = len;
        } else {
            l = len + o.lftEndOff() - lftEndOff();
        }
        return new DupRange(lftStartOff, rhtStartOff, l);
    }

    @Override
    public int compareTo(@NotNull DupRange o) {
        if (lftStartOff == o.lftStartOff) {
            return rhtStartOff - o.rhtStartOff;
        }
        return lftStartOff - o.lftStartOff;
    }

    public DupRange swap() {
        if (lftStartOff > rhtStartOff) {
            return new DupRange(rhtStartOff, lftStartOff, len);
        }
        return this;
    }
}
