package upt.baker.pdup.regex;

import upt.baker.pdup.PdupToken;
import upt.baker.pdup.regex.parser.ReParser;
import upt.baker.pdup.regex.vm.Inst;
import upt.baker.pdup.regex.vm.MatchGroups;
import upt.baker.pdup.regex.vm.ReInstVisitor;
import upt.baker.pdup.regex.vm.ReVm;

import java.util.ArrayList;
import java.util.List;

public class ReMatcher {
    private final ReVm vm;

    public ReMatcher(String re) {
        var prog = new ArrayList<Inst>();
        ReParser parser = new ReParser();
        ReInstVisitor compiler = new ReInstVisitor();
        parser.build(".*? & (" + re + ")").accept(compiler, prog);
        prog.add(new Inst.MatchInst());
        vm = new ReVm();
        vm.setProg(prog);
    }

    public List<MatchGroups.Group> findAll(List<PdupToken> target) {
        var t = target;
        var l = vm.match(t);
        var found = new ArrayList<MatchGroups.Group>();
        while (l.size() > 1) {
            var g = l.get(1);
            found.add(g);
            t = t.subList(g.start() + g.len(), t.size());
            l = vm.match(t);
        }

        int sz = found.size();
        for (int i = 1; i < sz; i++) {
            var prev = found.get(i - 1);
            int off = prev.start() + prev.len();
            var crt = found.get(i);
            found.set(i, new MatchGroups.Group(crt.start() + off, crt.len()));
        }

        return found;
    }
}
