package upt.baker.pdup.regex.vm;

import upt.baker.pdup.PdupToken;

import java.util.*;

public class ReVm {
    private final List<Inst> prog;
    private ThreadList clist;
    private ThreadList nlist;


    public ReVm(List<Inst> prog) {
        this.prog = prog;
        this.clist = new ThreadList(new ArrayDeque<>(prog.size()), new boolean[prog.size()]);
        this.nlist = new ThreadList(new ArrayDeque<>(prog.size()), new boolean[prog.size()]);
    }


    private void addThread(ThreadList l, VmThread t, int sp) {
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

    public List<MatchGroups.Group> match(List<PdupToken> input) {
        clist.clear();
        nlist.clear();

        addThread(clist, new VmThread(0, new int[20]), 0);
        VmThread matched = null;
        int sz = input.size();
        int pc;
        for (int sp = 0; sp < sz + 1; sp++) {
            while (!clist.isEmpty()) {
                var t = clist.removeFirst();
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

    private record ThreadList(Deque<VmThread> l, boolean[] mask) {
        public void add(VmThread t) {
            mask[t.pc] = true;
            l.add(t);
        }

        public void clear() {
            l.clear();
            Arrays.fill(mask, false);
        }

        public boolean isEmpty() {
            return l.isEmpty();
        }

        public boolean contains(VmThread t) {
            return mask[t.pc];
        }

        public VmThread removeFirst() {
            var t = l.removeFirst();
            mask[t.pc] = false;
            return t;
        }
    }

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
            return pc;
        }

        @Override
        public String toString() {
            return "VmThread{" +
                    "pc=" + pc +
                    ", groups=" + Arrays.toString(groups) +
                    '}';
        }
    }
}
