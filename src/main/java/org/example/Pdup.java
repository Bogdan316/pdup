package org.example;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StreamTokenizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: use hashing
// TODO: balanced tree
// TODO: better solution than the atomic integer
// TODO: lists should be sorted

public class Pdup {
    //    public static String S = "abczzeeabcwwwabcqweqweabc$";
    //    public static String S = "abczzeeabcwwabcqweqweabc$";
    //    public static String S = "qwewqwwe$";
    //    public static String S = "abcbcabc$";
    public static String S = "ubvbubv$";
    //    public static String S = "xbyyxbx$";
    public static int[] P = prev(S.toCharArray());
    public static int[] A = rev(P);

    static class Node {
        public int firstpos = -1;
        public int plen = 0;
        public int arclen = 0;
        public int i = -1;
        public @Nullable Node parent = null;
        public @Nullable Node sl = null;

        public @Nullable Node child = null;

        public @Nullable Node sibling = null;
        public @Nullable Node min = null;

        public String psub() {
            if (arclen >= 0 && firstpos >= 0 && plen >= 0) {
                String s = "";
                for (int i = firstpos, k = plen - arclen; k < plen; i++, k++) {
                    int b = f(P[i], k);
                    if (b >= 'a' && b <= 'z' || b == '$') {
                        s += (char) b;
                    } else {
                        s += b;
                    }
                }

                return s;
            }

            return "r";
        }

        @Override
        public String toString() {
            if (arclen >= 0 && firstpos >= 0 && plen >= 0) {
                var s = psub();
                String fullpath = s;
                var crt = this.parent;
                while (crt != null) {
                    fullpath = crt.psub() + fullpath;
                    crt = crt.parent;
                }
                return s + " fullpath: " + fullpath.substring(1);
            }
            return "r";
        }

        public void toGraphViz() {
            var child = this.child;
            System.out.println(hashCode() + "[label = \"" + psub() + "\"]");
            while (child != null) {
                System.out.println(hashCode() + "->" + child.hashCode());
                child.toGraphViz();
                child = child.sibling;
            }
            if (sl != null) {
                System.out.println(hashCode() + "->" + sl.hashCode() + "[style = dotted]");
            }
        }
    }


    public static Node root = new Node();

    public static int[] prev(char[] chr) {
        var res = new int[chr.length];
        var occur = new int[26];
        Arrays.fill(occur, -1);

        for (int i = 0; i < chr.length; i++) {
            if (chr[i] == 'u' || chr[i] == 'v' || chr[i] == 'x' || chr[i] == 'y') {
                int idx = chr[i] - 'a';
                int crtOccur = occur[idx];

                res[i] = crtOccur < 0 ? 0 : i - crtOccur;
                occur[idx] = i;
            } else {
                res[i] = chr[i];
            }
        }

        return res;
    }

    public static int f(int b, int j) {
        if (b >= 'a' && b <= 'z' || b == '$') {
            return b;
        }
        if (b > j) {
            return 0;
        }

        return b;
    }

    public static void insertChild(Node parent, Node son) {
        son.parent = parent;

        if (parent.child == null) {
            parent.child = son;
            return;
        }

        var sibling = parent.child;
        while (sibling.sibling != null) {
            sibling = sibling.sibling;
        }

        sibling.sibling = son;
    }

    public static void removeChild(Node v, Node w) {
        if (v.child == null) {
            return;
        }

        if (v.child == w) {
            v.child = v.child.sibling;
            w.sibling = null;
        } else {
            var u = v.child;
            while (u.sibling != null) {
                if (u.sibling == w) {
                    u.sibling = w.sibling;
                    w.sibling = null;
                    return;
                }
                u = u.sibling;
            }
        }
    }

    public static Node findOutgoingEdge(Node v, int i, int j) {
        var n = v.child;
        while (n != null && f(P[n.firstpos], n.plen - n.arclen) != f(P[i + j], j)) {
            n = n.sibling;
        }

        return n;
    }

    // s is the plen of head i - 1
    public static Node rescan(@NotNull Node start, int i, int j, int s) {
        if (s == 0) {
            return start;
        }
        var w = findOutgoingEdge(start, i, j);
        if (w == null) {
            return start;
        }

        if (w.plen >= s) {
            return w;
        } else {
            return rescan(w, i, j + w.arclen, s);
        }
    }


    public static Node get0Edge(Node v) {
        var n = v.child;
        while (n != null && f(P[n.firstpos], n.plen - n.arclen) != 0) {
            n = n.sibling;
        }

        return n;
    }

    public static void update(int i, Node g, Node newhd, Node oldchild) {
        if (g.min == null || g.min.plen > 1 + newhd.plen) {
            newhd.min = null;
        } else {
            newhd.min = g.min;
            var crt = g.min;
            Node lst = null;
            while (crt != null && crt.plen <= 1 + newhd.plen) {
                crt.sl = newhd;
                lst = crt;
                if (crt.i == i - 1) {
                    crt = oldchild;
                } else {
                    // TODO: this should be O(1)
                    crt = get0Edge(crt);
                }
            }

            if (lst != null && lst.child != null && lst.sl == g) {
                g.min = lst;
            } else {
                g.min = null;
            }
        }
    }

    public static Node scan(Node start, int i, int j, AtomicInteger splitPoint) {
        var g = findOutgoingEdge(start, i, j);
        if (g == null) {
            return start;
        } else {
            int k = g.firstpos;
            for (; k < g.firstpos + g.arclen; k++, j++) {
                if (f(P[k], k) != f(P[i + j], j)) {
                    splitPoint.set(k);
                    return g;
                }
            }
            return scan(g, i, j, splitPoint);
        }
    }

    public static Node scan(Node g, int i, int j, int off, AtomicInteger splitPoint) {
        if (g == root) {
            assert (off == 0);
            return scan(g, i, j, splitPoint);
        }
        // subtract the path length so far, the path length to g
        off -= g.plen - g.arclen;
        int k = g.firstpos + off;
        // the path to g is fully scanned
        if (k == g.firstpos + g.arclen) {
            return scan(g, i, j, splitPoint);
        }

        assert k < g.firstpos + g.arclen;

        for (; k < g.firstpos + g.arclen; k++, j++) {
            if (f(P[k], k) != f(P[i + j], j)) {
                splitPoint.set(k);
                return g;
            }
        }
        return scan(g, i, j, splitPoint);
    }

    public static int lc(TreeSet<Integer> pl) {
        if (pl.isEmpty()) {
            return -1;
        }
        if (pl.getFirst() == 0) {
            return 0;
        }

        return P[pl.getFirst() - 1];
    }

    public static int start(Node v) {
        return v.firstpos + v.arclen - v.plen;
    }

    public static int[] rev(int[] prev) {
        var rev = new int[prev.length];
        for (int i = 0; i < prev.length; i++) {
            if (Character.isAlphabetic(prev[i]) || prev[i] == '$') {
                rev[i] = prev[i];
            } else {
                if (prev[i] != 0) {
                    int p = prev[i];
                    if (rev[i - p] == 0) {
                        rev[i - p] = p;
                    }
                    rev[i] = 0;
                }
            }
        }

        return rev;
    }

    public static int lca(List<Integer> pl) {
        if (pl.isEmpty()) {
            return -1;
        }
        var first = pl.getFirst();
        if (first == 0) {
            return 0;
        }

        return A[first - 1];
    }

    public static List<List<Integer>> pcombine(List<List<Integer>> cl1, List<List<Integer>> cl2, int len, int t) {
        var outputlist = new ArrayList<List<Integer>>();
        if (len < t) {
            return new ArrayList<>();
        }

        for (var pl1 : cl1) {
            for (var pl2 : cl2) {
                if (f(lca(pl1), len + 1) != f(lca(pl2), len + 1)) {
                    for (var p1 : pl1) {
                        for (var p2 : pl2) {
                            System.out.println(p1 + " " + p2 + " " + len);
                            System.out.print("\t");
                            for (int i = p1; i < p1 + len; i++) {
                                System.out.print(S.charAt(i));
                            }
                            System.out.print("\n\t");
                            for (int i = p2; i < p2 + len; i++) {
                                System.out.print(S.charAt(i));
                            }
                            System.out.println("\n");
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

    public static List<List<Integer>> concatz(List<List<Integer>> cl, int len) {
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

    public static List<List<Integer>> pdup(Node v, int t) {
        if (v.child == null) {
            var tmp = new ArrayList<List<Integer>>();
            tmp.add(new ArrayList<>(List.of(start(v))));
            return tmp;
        }

        List<List<Integer>> cl = new ArrayList<>();
        var s = v.child;
        while (s != null) {
            cl = pcombine(cl, concatz(pdup(s, t), v.plen), v.plen, t);
            s = s.sibling;
        }

        return cl;
    }

    public static void main(String[] args) {
        root.sl = root;
        var oldhd = root;
        Node oldchild = null;
        Node newhd;
        Node g;

        for (int i = 0; i < P.length; i++) {
            // phase 1
            if (oldhd.sl == null) {
                // only the root does not have a parent, but the root always has a sl
                assert (oldhd.parent != null);
                // all oldhds will pass through this if and get a sl
                assert (oldhd.parent.sl != null);

                oldhd.sl = rescan(oldhd.parent.sl, i, 0, oldhd.plen - 1);
            }

            // phase 2
            var k = new AtomicInteger(-1);
            if (oldhd == root) {
                g = scan(root, i, 0, k);
            } else {
                int off = oldhd.plen - 1;
                g = scan(oldhd.sl, i, off, off, k);
            }

            // phase 3
            if (k.get() != -1) {
                int idx = k.get() - g.firstpos;
                newhd = new Node();
                newhd.firstpos = g.firstpos;
                newhd.plen = g.plen - g.arclen + idx;
                newhd.arclen = idx;
                newhd.child = g;
                newhd.i = i;

                g.firstpos += newhd.arclen;
                g.arclen -= newhd.arclen;

                // if g does not have a parent then a split should not be possible
                assert (g.parent != null);
                removeChild(g.parent, g);
                insertChild(g.parent, newhd);
                g.parent = newhd;
            } else {
                newhd = g;
            }

            update(i, g, newhd, oldchild);
            // oldhd.sl is updated in the beginning of the loop
            assert (oldhd.sl != null);
            if (newhd.plen < oldhd.sl.plen) {
                oldhd.sl = newhd;
            }

            // phase 4
            if (oldhd.sl.min == null || oldhd.sl.min.plen > oldhd.plen) {
                oldhd.sl.min = oldhd;
            }

            // phase 5
            var leaf = new Node();
            leaf.parent = newhd;
            leaf.firstpos = newhd == root ? i : i + newhd.plen;
            leaf.plen = P.length - i;
            leaf.arclen = leaf.plen - newhd.plen;
            insertChild(newhd, leaf);

            // phase 6
            oldhd = newhd;
            oldchild = g;
        }

        System.out.println(pdup(root, 1));
    }
}
