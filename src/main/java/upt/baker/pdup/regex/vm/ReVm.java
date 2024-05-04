package upt.baker.pdup.regex.vm;


import upt.baker.pdup.PdupToken;

import java.util.*;

public class ReVm {
    private List<Inst> prog = List.of();

    private record VmThread(int pc, int[] groups) {
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
        public String toString() {
            return "VmThread{" +
                    "pc=" + pc +
                    ", groups=" + Arrays.toString(groups) +
                    '}';
        }
    }


    private void addThread(Set<VmThread> l, VmThread t, int sp) {
        if (l.contains(t)) {
            return;
        }
        var i = prog.get(t.pc());
        if (i instanceof Inst.JmpInst j) {
            addThread(l, new VmThread(j.label, t.groups()), sp);
        } else if (i instanceof Inst.SplitInst si) {
            addThread(l, new VmThread(si.l1, t.groups()), sp);
            addThread(l, new VmThread(si.l2, t.groups()), sp);
        } else if (i instanceof Inst.SaveInst sv) {
            var g = Arrays.copyOf(t.groups(), t.groups().length);
            g[sv.saveIdx] = sp;
            addThread(l, new VmThread(t.pc() + 1, g), sp);
        } else {
            l.add(t);
        }
    }

    private VmThread removeFirst(Set<VmThread> s) {
        var e = s.iterator().next();
        s.remove(e);
        return e;
    }

    public List<MatchGroups.Group> match(List<PdupToken> input) {
        var clist = new LinkedHashSet<VmThread>();
        var nlist = new LinkedHashSet<VmThread>();

        addThread(clist, new VmThread(0, new int[20]), 0);
        VmThread matched = null;
        int sz = input.size();
        int pc;
        for (int sp = 0; sp < sz + 1; sp++) {
            while (!clist.isEmpty()) {
                var t = removeFirst(clist);
                pc = t.pc();
                var i = prog.get(pc);
                if (i instanceof Inst.LitInst l) {
                    if (sp < sz && l.isMatching(input.get(sp).idx)) {
                        addThread(nlist, new VmThread(pc + 1, t.groups()), sp + 1);
                    }
                } else if (i instanceof Inst.AnyInst) {
                    addThread(nlist, new VmThread(pc + 1, t.groups()), sp + 1);
                } else if (i instanceof Inst.MatchInst) {
                    matched = t;
                    break;
                }
            }

            var tmp = clist;
            clist = nlist;
            nlist = tmp;
            nlist.clear();
        }

        if (matched == null) {
            return List.of();
        } else {
            return new MatchGroups(matched.groups()).getGroups();
        }
    }

    public void setProg(List<Inst> prog) {
        this.prog = prog;
    }
}
