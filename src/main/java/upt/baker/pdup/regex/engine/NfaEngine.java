package upt.baker.pdup.regex.engine;

import upt.baker.pdup.regex.parser.ReParser;
import upt.baker.pdup.regex.parser.ReToken;

import java.util.*;

import static upt.baker.pdup.regex.engine.State.*;


public class NfaEngine {
    record Frag(State start, List<Transition> out) {
        void patch(State state) {
            for (var t : out) {
                t.setDst(state);
            }
        }
    }

    public State buildNfa(List<ReToken> postfix) {
        var stack = new ArrayDeque<Frag>();
        for (var t : postfix) {
            switch (t.type()) {
                case AND: {
                    var e2 = stack.pop();
                    var e1 = stack.pop();
                    e1.patch(e2.start());
                    stack.push(new Frag(e1.start(), e2.out()));
                    break;
                }
                case OR: {
                    var e2 = stack.pop();
                    var e1 = stack.pop();
                    var s = new Split(e1.start(), e2.start());
                    var out = new ArrayList<>(e1.out());
                    out.addAll(e2.out());
                    stack.push(new Frag(s, out));
                    break;
                }
                case HOOK: {
                    var e = stack.pop();
                    var s = new Split(e.start(), null);
                    var out = new ArrayList<>(e.out());
                    out.add(s.right);
                    stack.push(new Frag(s, out));
                    break;
                }
                case STAR: {
                    var e = stack.pop();
                    var s = new Split(e.start(), null);
                    e.patch(s);
                    stack.push(new Frag(s, List.of(s.right)));
                    break;
                }
                case PLUS: {
                    var e = stack.pop();
                    var s = new Split(e.start(), null);
                    e.patch(s);
                    stack.push(new Frag(e.start(), List.of(s.right)));
                    break;
                }
                case DOT: {
                    var s = new Any();
                    stack.push(new Frag(s, List.of(s.out)));
                    break;
                }
                case TERM: {
                    var s = new Literal(t.value());
                    stack.push(new Frag(s, List.of(s.out)));
                    break;
                }
                default: throw new IllegalStateException();
            }
        }

        var e = stack.pop();
        e.patch(new Match());
        return e.start();
    }

    public boolean fullMatch(State start, List<String> s) {
        Set<State> clist = new HashSet<>();
        Set<State> nlist = new HashSet<>();

        start.addState(clist, "");
        for (var c : s) {
            step(clist, c, nlist);

            clist = nlist;
            nlist = new HashSet<>();
        }

        return isMatch(clist);
    }

    public boolean isMatch(Set<State> l) {
        for (var s : l) {
            if (s instanceof Match) {
                return true;
            }
        }
        return false;
    }

    public void step(Set<State> clist, String c, Set<State> nlist) {
        for (var s : clist) {
            s.addState(nlist, c);
        }
    }

    public static void main(String[] args) {
        var tokens = new ReParser(".* & C & .*").postOrder();
        System.out.println(tokens);
        var toks = List.<String>of("C");
        var engine = new NfaEngine();
        var re = engine.buildNfa(tokens);
        System.out.println(engine.fullMatch(re, toks));
        System.exit(0);
    }
}
