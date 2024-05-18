package upt.baker.pdup.regex.parser;

import org.jetbrains.annotations.Nullable;
import upt.baker.pdup.utils.KeywordMapping;

import java.util.ArrayList;
import java.util.List;

import static upt.baker.pdup.regex.parser.ReExpNode.*;
import static upt.baker.pdup.regex.parser.ReToken.ReTokenType.*;

public class ReParser {
    private int i = 0;
    private int groupIdx = 0;
    private List<ReToken> tokens = List.of();

    public ReExpNode build(String re) {
        i = 0;
        groupIdx = 0;
        tokens = tokenize(re);
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
            int idx = groupIdx++;
            var e = new GroupExp(expr(), idx);
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
            case STAR -> {
                var h = advance();
                if (h == null) {
                    yield new StarExp(lastNode);
                }
                if (h.type() == HOOK) {
                    yield new StarHookExp(lastNode);
                }
                i--;
                yield new StarExp(lastNode);
            }
            case PLUS -> new PlusExp(lastNode);
            case HOOK -> new HookExp(lastNode);
            default -> throw new IllegalStateException();
        };

        return fFactor(u);
    }

    private List<ReToken> tokenize(String re) {
        var toks = new ArrayList<ReToken>();
        // wrap in parans to have group 0 (full match group)
        toks.add(ReToken.opFromChar('('));

        var term = new StringBuilder();

        for (var c : (re + " ").toUpperCase().toCharArray()) {
            if (Character.isLetter(c) || c == '_') {
                term.append(c);
            } else {
                if (!term.isEmpty()) {
                    var t = term.toString();
                    Integer idx = KeywordMapping.getIndex(t);
                    if (idx == null) {
                        throw new UnkReTokenException(t);
                    }
                    toks.add(new ReToken(TERM, idx));
                    term = new StringBuilder();
                }
                if (!Character.isWhitespace(c)) {
                    toks.add(ReToken.opFromChar(c));
                }
            }
        }

        toks.add(ReToken.opFromChar(')'));
        return toks;
    }
}
