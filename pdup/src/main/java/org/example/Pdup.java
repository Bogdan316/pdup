package org.example;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: use hashing
// TODO: balanced tree
// TODO: better solution than the atomic integer

public class Pdup {
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
    }

    //    public static int[] P = new int[]{'a', 0, 'a', 'b', 3, '$'};
    public static int[] P = new int[]{0, 'b', 0, 'b', 4, 'b', 4, '$'};
//    public static int[] P = new int[]{'a', 'b', 'a', 'b', 'c'};
//    public static int[] P = new int[]{'a', 'b', 'c', 'b', 'c', 'a', 'b', 'c', '$'};
//    public static int[] P = new int[]{0, 'b', 0, 1, 4, 'b', 2, '$'};

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
        var w = findOutgoingEdge(start, i, 0);
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
            for (; k < g.plen; k++, j++) {
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
        int k = g.firstpos + off;
        if (k >= g.plen) {
            return g;
        }
        for (; k < g.plen; k++, j++) {
            if (f(P[k], k) != f(P[i + j], j)) {
                splitPoint.set(k);
                return g;
            }
        }
        return scan(g, i, j, splitPoint);
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
                g = scan(oldhd.sl, i, oldhd.plen - 1, oldhd.plen - 1, k);
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
        System.out.println();
    }
}
