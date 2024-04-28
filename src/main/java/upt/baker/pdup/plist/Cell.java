package upt.baker.pdup.plist;

import it.unimi.dsi.fastutil.ints.IntIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Cell extends PList {
    private final int value;

    public Cell(int value) {
        this.value = value;
    }

    @Override
    public int getFirst() {
        return value;
    }

    @Override
    public @NotNull Iterator<Integer> iterator() {
        return new It(value);
    }

    private static class It implements Iterator<Integer> {
        private final int value;

        public It(int value) {
            this.value = value;
        }

        private boolean hasVal = true;

        @Override
        public Integer next() {
            if (!hasVal) {
                throw new NoSuchElementException();
            }
            hasVal = false;
            return value;
        }

        @Override
        public boolean hasNext() {
            return hasVal;
        }
    }
}
