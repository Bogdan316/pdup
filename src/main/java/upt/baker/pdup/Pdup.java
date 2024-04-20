package upt.baker.pdup;


// TODO: balanced tree
// TODO: better solution than the atomic integer
// TODO: lists should be sorted


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class Pdup<T extends PdupToken> {
    public final int[] P;
    public final int[] A;
    public final Node root;
    private final Function<Integer, BiConsumer<Integer, Integer>> matchConsumer;
    private final List<T> params;

    public Pdup(List<T> params, int maxIdx, Function<Integer, BiConsumer<Integer, Integer>> matchConsumer) {
        this.params = params;
        this.matchConsumer = matchConsumer;
        P = prev(params, maxIdx);
        A = rev(P);

        root = new Node();
        root.setSl(root);
    }

    public int[] prev(List<T> params, int maxIdx) {
        var res = new int[params.size()];
        var occur = new int[maxIdx];
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

    public int[] rev(int[] prev) {
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

    public int f(int b, int j) {
        if (b < 0) {
            return b;
        }
        if (b > j) {
            return 0;
        }

        return b;
    }

    public int lca(List<Integer> pl) {
        if (pl.isEmpty()) {
            return -1;
        }
        var first = pl.get(0);
        if (first == 0) {
            return 0;
        }

        return A[first - 1];
    }

    public Node rescan(@NotNull Node start, int i, int j, int s) {
        if (s == 0) {
            return start;
        }
        // TODO: bug(1) this needs to be checked if it is correct in phase 2
        //   this fixes issues for ConvertToClsSet.java and ConvertArscFile.java, starting path is already parcurs ...
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
        // TODO: pos for bug 2, not sure if need to put 1 back, seems no
        if (g.getMin() == null || g.getMin().getPathLen() > 1 + newhd.getPathLen()) {
            newhd.setMin(null);
        } else {
            newhd.setMin(g.getMin());
            var crt = g.getMin();
            Node lst = null;
            while (crt != null && crt.getPathLen() <= 1 + newhd.getPathLen()) {
                crt.setSl(newhd);
                lst = crt;
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
//            var s = "Suffix " + i + " --> ";
//            for (int j = i; j < P.length; j++) {
//                var p = params.get(j).toString();
//                s += p;
//            }
//            System.out.println(s);
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

                // TODO: bug 2, i == 999 (sl for oldhead is not the smallest caused by the fact that that sl's min is not the smallest)
                //  -> paper suggests this should happen only after a split:
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
//            var tmp = "Result " + i + " --> " + leaf.toString().split("~FULL_PATH~: ")[1];
//            System.out.println(tmp);
//            var p1 = tmp.split(" --> ")[1].split("\\|");
//            var p2 = s.split(" --> ")[1].split("\\|");
            // TODO: naive check, do the real one
//            for (int l = 0; l < p1.length; l++) {
//                if (!p1[l].equals(p2[l])) {
//                    throw new IllegalStateException("Not matching for suffix: " + i + " at pos: " + l);
//                }
//            }


            // phase 6
            oldhd = newhd;
            oldchild = g;
        }
    }

    public List<List<Integer>> pcombine(List<List<Integer>> cl1, List<List<Integer>> cl2, int len, int t) {
        var outputlist = new ArrayList<List<Integer>>();
        if (len < t) {
            return new ArrayList<>();
        }

        for (var pl1 : cl1) {
            for (var pl2 : cl2) {
                if (f(lca(pl1), len + 1) != f(lca(pl2), len + 1)) {
                    for (var p1 : pl1) {
                        for (var p2 : pl2) {
                            matchConsumer.apply(len).accept(p1, p2);
                        }
                    }
                }
            }
        }

        var usedcl1 = new ArrayList<List<Integer>>();
        var usedcl2 = new ArrayList<List<Integer>>();

        for (var pl1 : cl1) {
            for (var pl2 : cl2) {
                if (f(lca(pl1), len + 1) == f(lca(pl2), len + 1)) {
                    var newpl = new ArrayList<>(pl1);
                    newpl.addAll(pl2);
                    outputlist.add(newpl);

                    usedcl1.add(pl1);
                    usedcl2.add(pl2);
                }
            }
        }

        for (var pl1 : cl1) {
            if (!usedcl1.contains(pl1)) {
                outputlist.add(pl1);
            }
        }

        for (var pl2 : cl2) {
            if (!usedcl2.contains(pl2)) {
                outputlist.add(pl2);
            }
        }

        return outputlist;
    }

    public List<List<Integer>> concatz(List<List<Integer>> cl, int len) {
        var concat = new ArrayList<Integer>();
        var toRemove = new ArrayList<List<Integer>>();
        for (var pl : cl) {
            if (f(lca(pl), len + 1) == 0) {
                concat.addAll(pl);
                toRemove.add(pl);
            }
        }

        for (var pl : toRemove) {
            cl.remove(pl);
        }
        cl.add(concat);

        return cl;
    }

    private List<List<Integer>> pdup(Node v, int t) {
        if (v.isLeaf()) {
            var tmp = new ArrayList<List<Integer>>();
            tmp.add(new ArrayList<>(List.of(v.start())));
            return tmp;
        }

        List<List<Integer>> cl = new ArrayList<>();
        for (var s : v.getChildren()) {
            cl = pcombine(cl, concatz(pdup(s, t), v.getPathLen()), v.getPathLen(), t);
        }

        return cl;
    }

    public void pdup() {
        pdup(root, 200);
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
        private final Map<Integer, Node> children = new HashMap<>();

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public Collection<Node> getChildren() {
            return children.values();
        }

        public int start() {
            return getFirstPos() + getArcLen() - getPathLen();
        }

        private int getFirstSymbol() {
            return f(P[getFirstPos()], getPathLen() - getArcLen());
        }

        public void insertChild(Node child) {
            child.setParent(this);
            var first = child.getFirstSymbol();
            assert !children.containsKey(first);
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
                String s = "";
                for (int i = getFirstPos(); i < getFirstPos() + getArcLen(); i++) {
                    s += params.get(i);
                }

                return s;
            }

            return "r";
        }

        @Override
        public String toString() {
            if (getArcLen() >= 0 && getFirstPos() >= 0 && getPathLen() >= 0) {
                var s = psub();
                String fullpath = s;
                var crt = this.getParent();
                while (crt != null) {
                    fullpath = crt.psub() + fullpath;
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
