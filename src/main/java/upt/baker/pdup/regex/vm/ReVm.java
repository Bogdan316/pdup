package upt.baker.pdup.regex.vm;

import org.jetbrains.annotations.NotNull;
import upt.baker.pdup.regex.parser.ReParser;

import java.util.*;

public class ReVm {
    record VmThread(int pc, int[] groups) implements Comparable<VmThread> {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VmThread vmThread = (VmThread) o;
            return pc == vmThread.pc;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pc);
        }

        @Override
        public int compareTo(@NotNull ReVm.VmThread o) {
            return pc - o.pc();
        }
    }

    private static boolean run(List<Inst> prog, List<String> input) {
        var clist = new TreeSet<VmThread>();
        var nlist = new TreeSet<VmThread>();
        clist.add(new VmThread(0, new int[20]));
        boolean matched = false;
        int pc;
        for (int sp = 0; sp < input.size(); sp++) {
            VmThread t;
            while ((t = clist.pollFirst()) != null) {
                pc = t.pc();
                var i = prog.get(pc);
                if (i instanceof Inst.LitInst l) {
                    if (l.val().equals(input.get(sp))) {
                        nlist.add(new VmThread(pc + 1, t.groups()));
                    }
                } else if (i instanceof Inst.MatchInst) {
                    matched = true;
                    System.out.println(Arrays.toString(t.groups()));
                    break;
                } else if (i instanceof Inst.JmpInst j) {
                    clist.add(new VmThread(j.label, t.groups));
                } else if (i instanceof Inst.SplitInst si) {
                    clist.add(new VmThread(si.l1, t.groups()));
                    clist.add(new VmThread(si.l2, t.groups()));
                } else if (i instanceof Inst.SaveInst sv) {
                   t.groups()[sv.saveIdx]  = sp;
                   clist.add(new VmThread(pc + 1, t.groups()));
                } else {
                    throw new IllegalStateException();
                }
            }

            var tmp = clist;
            clist = nlist;
            nlist = tmp;
            nlist.clear();
        }

        return matched;
    }

    public static void main(String[] args) {
        var vis = new ReInstVisitor();
        var insts = new ArrayList<Inst>();
        new ReParser("(a+) & (b+)").build().accept(vis, insts);
        insts.add(new Inst.MatchInst());
        for (int i = 0; i < insts.size(); i++) {
            System.out.println(i + " -> " + insts.get(i));
        }
        System.out.println(run(insts, List.of("A", "A", "A", "B", "B", "B", "C", "\0")));

    }
}
