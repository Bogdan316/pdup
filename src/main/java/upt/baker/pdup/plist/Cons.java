package upt.baker.pdup.plist;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class Cons extends PList {
    private final PList first;
    private final PList second;

    public Cons(PList first, PList second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int getFirst() {
        return first.getFirst();
    }

    @Override
    public @NotNull Iterator<Integer> iterator() {
        return new It(first.iterator(), second.iterator());
    }

    private static class It implements Iterator<Integer> {
        private final Iterator<Integer> firstIt;
        private final Iterator<Integer> secondIt;

        public It(Iterator<Integer> firstIt, Iterator<Integer> secondIt) {
            this.firstIt = firstIt;
            this.secondIt = secondIt;
        }

        @Override
        public Integer next() {
            if (firstIt.hasNext()) {
                return firstIt.next();
            }
            return secondIt.next();
        }

        @Override
        public boolean hasNext() {
            return firstIt.hasNext() || secondIt.hasNext();
        }
    }
}
