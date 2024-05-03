package upt.baker.pdup.regex.parser;

import org.jetbrains.annotations.Nullable;

import static upt.baker.pdup.regex.parser.ReToken.ReTokenType.*;
import static upt.baker.pdup.regex.parser.ReExpNode.*;

import java.util.ArrayList;
import java.util.List;

public class ReParser {
    private int i = 0;
    private int groupIdx = 1;
    private final List<ReToken> tokens;

    public ReParser(String exp) {
        this.tokens = tokenize(exp);
    }

    public ReExpNode build() {
        return expr();
    }

    private @Nullable ReToken advance() {
        if (i < tokens.size()) {
            return tokens.get(i++);
        }

        return null;
    }

    private ReExpNode expr() {
        var left = term();
        while (true) {
            var op = advance();
            if (op == null) {
                break;
            }
            var t = op.type();
            if (t != OR) {
                i--;
                return left;
            }
            var right = term();
            left = new AltExp(left, right);
        }

        return left;
    }

    private ReExpNode term() {
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
            left = new ConcatExp(left, right);
        }

        return left;
    }


    private ReExpNode factor() {
        var tok = advance();
        if (tok == null) {
            throw new ReSyntaxError("Unexpected end of expression");
        }
        if (tok.type() == LPAREN) {
            var e = new GroupExp(expr(), groupIdx++);
            tok = advance();
            if (tok == null) {
                throw new ReSyntaxError("Expected ')' but reached the end of expression");
            }
            if (tok.type() != RPAREN) {
                throw new ReSyntaxError("Expected ')' but got '" + tok.value() + "'");
            }

            return fFactor(e);
        } else {
            ReExpNode t;
            if (tok.type() == TERM) {
                t = new LiteralExp(tok.value());
            } else if (tok.type() == DOT) {
                t = new AnyExp();
            } else {
                throw new IllegalStateException();
            }

            return fFactor(t);
        }
    }

    private ReExpNode fFactor(ReExpNode lastNode) {
        int init = i;
        var op = advance();
        if (op == null) {
            i = init;
            return lastNode;
        }

        var t = op.type();
        if (t != STAR && t != HOOK && t != PLUS) {
            i = init;
            return lastNode;
        }

        var u = switch (t) {
            case STAR -> new StarExp(lastNode);
            case PLUS -> new PlusExp(lastNode);
            case HOOK -> new HookExp(lastNode);
            default -> throw new IllegalStateException();
        };

        return fFactor(u);
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
        System.out.println(p.build());
    }
}
