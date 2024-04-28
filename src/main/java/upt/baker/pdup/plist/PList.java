package upt.baker.pdup.plist;


public abstract class PList implements Iterable<Integer> {
    private boolean marked = false;

    public void mark() {
        marked = true;
    }

    public void unmark() {
        marked = false;
    }

    public boolean isMarked() {
        return marked;
    }

    public abstract int getFirst();
}

