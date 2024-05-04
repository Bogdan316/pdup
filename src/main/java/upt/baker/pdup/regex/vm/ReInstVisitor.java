package upt.baker.pdup.regex.vm;

import upt.baker.pdup.regex.parser.ReExpNode.*;
import upt.baker.pdup.regex.parser.ReVoidAstVisitor;

import java.util.List;

public class ReInstVisitor implements ReVoidAstVisitor<List<Inst>> {
    @Override
    public void visit(StarExp exp, List<Inst> arg) {
        int label = arg.size();
        var split = new Inst.SplitInst();
        arg.add(split);
        split.l1 = arg.size();
        exp.exp().accept(this, arg);
        var jmp = new Inst.JmpInst();
        jmp.label = label;
        arg.add(jmp);
        split.l2 = arg.size();
    }

    @Override
    public void visit(StarHookExp exp, List<Inst> arg) {
        int label = arg.size();
        var split = new Inst.SplitInst();
        arg.add(split);
        split.l2 = arg.size();
        exp.exp().accept(this, arg);
        var jmp = new Inst.JmpInst();
        jmp.label = label;
        arg.add(jmp);
        split.l1 = arg.size();
    }

    @Override
    public void visit(PlusExp exp, List<Inst> arg) {
        int l1 = arg.size();
        exp.exp().accept(this, arg);
        var split = new Inst.SplitInst();
        split.l1 = l1;
        arg.add(split);
        split.l2 = arg.size();
    }

    @Override
    public void visit(HookExp exp, List<Inst> arg) {
        var split = new Inst.SplitInst();
        arg.add(split);
        split.l1 = arg.size();
        exp.exp().accept(this, arg);
        split.l2 = arg.size();
    }

    @Override
    public void visit(ConcatExp exp, List<Inst> arg) {
        exp.left().accept(this, arg);
        exp.right().accept(this, arg);
    }

    @Override
    public void visit(AltExp exp, List<Inst> arg) {
        var split = new Inst.SplitInst();
        arg.add(split);
        split.l1 = arg.size();
        exp.left().accept(this, arg);
        var jmp = new Inst.JmpInst();
        arg.add(jmp);
        split.l2 = arg.size();
        exp.right().accept(this, arg);
        jmp.label = arg.size();
    }

    @Override
    public void visit(GroupExp exp, List<Inst> arg) {
        var s = new Inst.SaveInst();
        int k = exp.groupIdx();
        s.saveIdx = 2 * k;
        arg.add(s);
        exp.exp().accept(this, arg);
        s = new Inst.SaveInst();
        s.saveIdx = 2 * k + 1;
        arg.add(s);
    }

    @Override
    public void visit(AnyExp exp, List<Inst> arg) {
        arg.add(new Inst.AnyInst());
    }

    @Override
    public void visit(LiteralExp exp, List<Inst> arg) {
        arg.add(new Inst.LitInst(exp.value()));
    }

}
