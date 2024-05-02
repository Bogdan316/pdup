package upt.baker.pdup.regex.parser;

import org.jetbrains.annotations.Nullable;

import static upt.baker.pdup.regex.parser.ReToken.ReTokenType.*;

import java.util.ArrayList;
import java.util.List;

public class ReParser {
    int i = 0;
    private final List<ReToken> tokens;

    public ReParser(String exp) {
        this.tokens = tokenize(exp);
    }

    private @Nullable ReToken advance() {
        if (i < tokens.size()) {
            return tokens.get(i++);
        }

        return null;
    }

    private AstNode expr() {
        var left = term();
        while (true) {
            var op = advance();
            if (op == null) {
                break;
            }
            var t = op.type();
            if (t != OR) {
                throw new ReSyntaxError("Expected '|' but got '" + op.value() + "'");
            }
            var right = term();
            left = new AstNode.BinaryExp(op, left, right);
        }

        return left;
    }

    private AstNode term() {
        var left = factor();
        while (true) {
            var op = advance();
            if (op == null) {
                break;
            }
            var t = op.type();
            if (t != AND) {
                i--;
                return left;
            }
            var right = factor();
            left = new AstNode.BinaryExp(op, left, right);
        }

        return left;
    }


    private AstNode factor() {
        var tok = advance();
        if (tok == null) {
            throw new ReSyntaxError("Unexpected end of expression");
        }
        if (tok.type() == LPAREN) {
            var e = term();
            tok = advance();
            if (tok == null) {
                throw new ReSyntaxError("Expected ')' but reached the end of expression");
            }
            if (tok.type() != RPAREN) {
                throw new ReSyntaxError("Expected ')' but got '" + tok.value() + "'");
            }

            var p = fFactor(e);
            return p == null ? e : p;
        } else {
            var t = new AstNode.Term(tok);
            var e = fFactor(t);
            return e == null ? t : e;
        }
    }

    private @Nullable AstNode fFactor(AstNode lastNode) {
        int init = i;
        var op = advance();
        if (op == null) {
            i = init;
            return null;
        }

        var t = op.type();
        if (t != STAR && t != HOOK && t != PLUS) {
            i = init;
            return null;
        }

        var u = new AstNode.UnaryExp(op, lastNode);
        var e = fFactor(u);
        return e == null ? u : e;
    }

    public List<ReToken> postOrder() {
        var order = new ArrayList<ReToken>();
        expr().postOrder(order);
        return order;
    }

    public List<ReToken> tokenize(String re) {
        var tokens = new ArrayList<ReToken>();
        var term = new StringBuilder();

        for (var c : (re + " ").toUpperCase().toCharArray()) {
            if (Character.isLetter(c) || c == '_') {
                term.append(c);
            } else {
                if (!term.isEmpty()) {
                    var t = term.toString();
//                    try {
//                        // check token exists as defined by intellij
//                        JavaTokenType.class.getField(t);
//                    } catch (NoSuchFieldException e) {
//                        throw new UnkReTokenException(t);
//                    }
                    tokens.add(new ReToken(TERM, term.toString()));
                    term = new StringBuilder();
                }
                if (!Character.isWhitespace(c)) {
                    tokens.add(ReToken.opFromChar(c));
                }
            }
        }

        return tokens;
    }

    public static void main(String[] args) {
        var p = new ReParser("a & (b & b)+ & a");
        System.out.println(p.postOrder());
    }
}
