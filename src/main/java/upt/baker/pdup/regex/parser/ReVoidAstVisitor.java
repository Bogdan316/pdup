package upt.baker.pdup.regex.parser;

import static upt.baker.pdup.regex.parser.ReExpNode.*;

public interface ReVoidAstVisitor<T> {
    void visit(StarExp exp, T arg);
    void visit(PlusExp exp, T arg);
    void visit(HookExp exp, T arg);
    void visit(ConcatExp exp, T arg);
    void visit(AltExp exp, T arg);
    void visit(GroupExp exp, T arg);
    void visit(AnyExp exp, T arg);
    void visit(LiteralExp exp, T arg);
}
