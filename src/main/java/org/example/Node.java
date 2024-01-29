package org.example;

import org.jetbrains.annotations.Nullable;

public class Node {
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
                int b = Pdup.f(Pdup.P[i], k);
                if (b < 0) {
                    s += Tokens.values()[-b] + " ";
                } else {
                    var idx = Lexer.S.get(i);
                    var id = Lexer.identifiers.entrySet().stream()
                            .filter(e -> e.getValue().equals(idx))
                            .findFirst().get().getKey();
                    if (id.contains(":")) {
                        id = id.split(":")[1];
                    }
                    s += id + " ";
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
