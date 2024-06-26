package upt.baker.pdup.duplications;

// TODO: balanced tree
// TODO: better solution than the atomic integer
// TODO: lists should be sorted


import com.carrotsearch.hppc.IntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import upt.baker.pdup.plist.Cell;
import upt.baker.pdup.plist.Cons;
import upt.baker.pdup.plist.PList;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Pdup {
    public final int[] P;
    public final int[] A;
    public final Node root;
    private final List<PdupToken> params;
    private final int tokenLen;
    // TODO: use in pcombine
    private final PriorityQueue<DupRange> dupRanges = new PriorityQueue<>();

    public Pdup(int tokenLen, List<PdupToken> params) {
        this.tokenLen = tokenLen;
        this.params = params;
        P = prev(params);
        A = rev(P);

        root = new Node();
        root.setSl(root);
    }

    public Node getTree() {
        return root;
    }

    public static int[] prev(List<PdupToken> params) {
        var res = new int[params.size()];
        int sz = params.stream().mapToInt(PdupToken::getIdx).max().orElse(-1);
        if (sz < 0) {
            sz = 0;
        }
        var occur = new int[sz + 1];
        Arrays.fill(occur, -1);

        for (int i = 0; i < params.size(); i++) {
            int idx = params.get(i).idx;
            if (idx >= 0) {
                int crtOccur = occur[idx];

                res[i] = crtOccur < 0 ? 0 : i - crtOccur;
                occur[idx] = i;
            } else {
                res[i] = idx;
            }
        }

        return res;
    }

    public static int[] rev(int[] prev) {
        var rev = new int[prev.length];
        for (int i = 0; i < prev.length; i++) {
            if (prev[i] < 0) {
                rev[i] = prev[i];
            } else if (prev[i] != 0) {
                int p = prev[i];
                if (rev[i - p] == 0) {
                    rev[i - p] = p;
                }
                rev[i] = 0;
            }
        }

        return rev;
    }

    public static int f(int b, int j) {
        if (b <= j) {
            return b;
        } else {
            return 0;
        }
    }

    public int lca(PList pl) {
        var first = pl.getFirst();
        if (first == 0) {
            return 0;
        }

        return A[first - 1];
    }

    public Node rescan(@NotNull Node start, int i, int j, int s) {
        if (s == 0) {
            return start;
        }
        if (start.getPathLen() >= s) {
            return start;
        }
        var w = start.findOutgoingEdge(i, j);
        if (w == null) {
            return start;
        }

        if (w.getPathLen() >= s) {
            return w;
        } else {
            return rescan(w, i, j + w.getArcLen(), s);
        }
    }


    public void update(int i, Node g, Node newhd, Node oldchild) {
        if (g.getMin() == null || g.getMin().getPathLen() > 1 + newhd.getPathLen()) {
            newhd.setMin(null);
        } else {
            newhd.setMin(g.getMin());
            var crt = g.getMin();
            while (crt != null && crt.getPathLen() <= 1 + newhd.getPathLen()) {
                crt.setSl(newhd);
                if (crt.getIdx() == i - 1) {
                    crt = oldchild;
                } else {
                    crt = crt.get0Edge();
                }
            }

            if (crt != null && !crt.isLeaf() && crt.getSl() == g) {
                g.setMin(crt);
            } else {
                g.setMin(null);
            }
        }
    }

    public Node scan(Node start, int i, int j, AtomicInteger splitPoint) {
        var g = start.findOutgoingEdge(i, j);
        if (g == null) {
            return start;
        } else {
            int k = g.getFirstPos();
            int p = g.getPathLen() - g.getArcLen();
            for (; k < g.getFirstPos() + g.getArcLen(); k++, j++, p++) {
                if (f(P[k], p) != f(P[i + j], j)) {
                    splitPoint.set(k);
                    return g;
                }
            }
            return scan(g, i, j, splitPoint);
        }
    }

    public Node scan(Node g, int i, int j, int off, AtomicInteger splitPoint) {
        if (g == root) {
            if (off != 0) {
                throw new IllegalStateException();
            }
            return scan(g, i, j, splitPoint);
        }
        int p = off;
        // the start offset should be on the edge entering g
        if (off < g.getPathLen() - g.getArcLen() || off > g.getPathLen()) {
            throw new IllegalStateException();
        }
        // subtract the path length so far, the path length to g
        off -= g.getPathLen() - g.getArcLen();
        int k = g.getFirstPos() + off;
        // the path to g is fully scanned so far
        if (k == g.getFirstPos() + g.getArcLen()) {
            return scan(g, i, j, splitPoint);
        }

        if (k > g.getFirstPos() + g.getArcLen()) {
            throw new IllegalStateException();
        }

        for (; k < g.getFirstPos() + g.getArcLen(); k++, j++, p++) {
            if (f(P[k], p) != f(P[i + j], j)) {
                splitPoint.set(k);
                return g;
            }
        }
        return scan(g, i, j, splitPoint);
    }

    public void build() {
        var oldhd = root;
        Node oldchild = null;
        Node newhd;
        Node g;

        for (int i = 0; i < P.length; i++) {
            // phase 1
            if (oldhd.getSl() == null) {
                // only the root does not have a parent, but the root always has a sl
                // all oldhds will pass through this if and get a sl
                assert Objects.requireNonNull(oldhd.getParent()).getSl() != null;
                oldhd.setSl(rescan(oldhd.getParent().getSl(), i, oldhd.getParent().getSl().getPathLen(), oldhd.getPathLen() - 1));
            }

            // phase 2
            var k = new AtomicInteger(-1);
            if (oldhd == root) {
                g = scan(root, i, 0, k);
            } else {
                int off = oldhd.getPathLen() - 1;
                g = scan(oldhd.getSl(), i, off, off, k);
            }

            // phase 3
            if (k.get() != -1) {
                int idx = k.get() - g.getFirstPos();
                newhd = new Node();
                newhd.setFirstPos(g.getFirstPos());
                newhd.setPathLen(g.getPathLen() - g.getArcLen() + idx);
                newhd.setArcLen(idx);
                newhd.setIdx(i);

                assert g.getParent() != null;
                g.getParent().removeChild(g);
                g.getParent().insertChild(newhd);

                g.setFirstPos(g.getFirstPos() + newhd.getArcLen());
                g.setArcLen(g.getArcLen() - newhd.getArcLen());

                // if g does not have a parent then a split should not be possible
                g.setParent(newhd);

                newhd.insertChild(g);

                update(i, g, newhd, oldchild);
                // oldhd.sl is updated in the beginning of the loop
                if (newhd.getPathLen() < oldhd.getSl().getPathLen()) {
                    oldhd.setSl(newhd);
                }
            } else {
                newhd = g;
            }

            // phase 4
            if (oldhd.getSl().getMin() == null || oldhd.getSl().getMin().getPathLen() > oldhd.getPathLen()) {
                oldhd.getSl().setMin(oldhd);
            }

            // phase 5
            var leaf = new Node();
            leaf.setParent(newhd);
            leaf.setFirstPos(i + newhd.getPathLen());
            leaf.setPathLen(P.length - i);
            leaf.setArcLen(leaf.getPathLen() - newhd.getPathLen());
            newhd.insertChild(leaf);

            // phase 6
            oldhd = newhd;
            oldchild = g;
        }
    }

    public List<PList> pcombine(List<PList> cl1, List<PList> cl2, int len, int t) {
        var outputlist = new ArrayList<PList>();
        if (len < t) {
            return new ArrayList<>();
        }

        for (var pl1 : cl1) {
            for (var pl2 : cl2) {
                if (f(lca(pl1), len + 1) != f(lca(pl2), len + 1)) {
                    for (var p1 : pl1) {
                        for (var p2 : pl2) {
                            dupRanges.add(new DupRange(p1, p2, len));
                        }
                    }
                } else {
                    outputlist.add(new Cons(pl1, pl2));

                    pl1.mark();
                    pl2.mark();
                }
            }
        }

        for (var pl1 : cl1) {
            if (pl1.isMarked()) {
                pl1.unmark();
            } else {
                outputlist.add(pl1);
            }
        }

        for (var pl2 : cl2) {
            if (pl2.isMarked()) {
                pl2.unmark();
            } else {
                outputlist.add(pl2);
            }
        }

        return outputlist;
    }

    public List<PList> concatz(List<PList> cl, int len) {
        PList concat = null;
        var out = new ArrayList<PList>();

        for (int i = cl.size() - 1; i >= 0; i--) {
            var pl = cl.remove(i);
            if (f(lca(pl), len + 1) == 0) {
                if (concat == null) {
                    concat = pl;
                } else {
                    concat = new Cons(concat, pl);
                }
            } else {
                out.add(pl);
            }
        }


        if (concat != null) {
            out.add(concat);
        }

        return out;
    }

    private List<PList> pdup(Node v, int t) {
        if (v.isLeaf()) {
            var tmp = new ArrayList<PList>();
            tmp.add(new Cell(v.start()));
            return tmp;
        }

        List<PList> cl = new ArrayList<>();
        for (var s : v.getChildren()) {
            cl = pcombine(cl, concatz(pdup(s.value, t), v.getPathLen()), v.getPathLen(), t);
        }

        return cl;
    }

    public PriorityQueue<DupRange> pdup() {
        build();
        pdup(root, tokenLen);
        return dupRanges;
    }

    public class Node {
        private int firstPos = -1;
        private int pathLen = 0;
        private int arcLen = 0;
        private int idx = -1;
        @Nullable
        private Node parent = null;
        @Nullable
        private Node sl = null;
        @Nullable
        private Node min = null;
        private final IntObjectHashMap<Node> children = new IntObjectHashMap<>(16);

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public IntObjectHashMap<Node> getChildren() {
            return children;
        }

        public int start() {
            return firstPos + arcLen - pathLen;
        }

        private int getFirstSymbol() {
            return f(P[firstPos], pathLen - arcLen);
        }

        public void insertChild(Node child) {
            child.setParent(this);
            var first = child.getFirstSymbol();
            children.put(first, child);
        }

        public void removeChild(Node w) {
            children.remove(w.getFirstSymbol());
        }

        public Node get0Edge() {
            return children.get(0);
        }

        public Node findOutgoingEdge(int i, int j) {
            return children.get(f(P[i + j], j));
        }

        public String psub() {
            if (getArcLen() >= 0 && getFirstPos() >= 0 && getPathLen() >= 0) {
                StringBuilder s = new StringBuilder();
                for (int i = getFirstPos(); i < getFirstPos() + getArcLen(); i++) {
                    s.append(params.get(i));
                }

                return s.toString();
            }

            return "r";
        }

        public List<Integer> getPsub() {
            var psub = new ArrayList<Integer>();
            int j = pathLen - arcLen;
            for (int i = firstPos; i < firstPos + arcLen; i++) {
                psub.add(f(P[i], j++));
            }

            return psub;
        }

        @Override
        public String toString() {
            if (getArcLen() >= 0 && getFirstPos() >= 0 && getPathLen() >= 0) {
                var s = psub();
                StringBuilder fullpath = new StringBuilder(s);
                var crt = this.getParent();
                while (crt != null) {
                    fullpath.insert(0, crt.psub());
                    crt = crt.getParent();
                }
                return s + " ~FULL_PATH~: " + fullpath.substring(1);
            }
            return "r";
        }

        public int getFirstPos() {
            return firstPos;
        }

        public void setFirstPos(int firstPos) {
            this.firstPos = firstPos;
        }

        public int getPathLen() {
            return pathLen;
        }

        public void setPathLen(int pathLen) {
            this.pathLen = pathLen;
        }

        public int getArcLen() {
            return arcLen;
        }

        public void setArcLen(int arcLen) {
            this.arcLen = arcLen;
        }

        public int getIdx() {
            return idx;
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }

        public @Nullable Node getParent() {
            return parent;
        }

        public void setParent(@Nullable Node parent) {
            this.parent = parent;
        }

        public @Nullable Node getSl() {
            return sl;
        }

        public void setSl(@Nullable Node sl) {
            this.sl = sl;
        }

        public @Nullable Node getMin() {
            return min;
        }

        public void setMin(@Nullable Node min) {
            this.min = min;
        }
    }
}
