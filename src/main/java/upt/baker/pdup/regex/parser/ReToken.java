package upt.baker.pdup.regex.parser;

import java.util.Map;

import static upt.baker.pdup.regex.parser.ReToken.ReTokenType.*;

public record ReToken(ReTokenType type, String value) {

    public enum ReTokenType {
        STAR,
        PLUS,
        HOOK,
        AND,
        OR,
        LPAREN,
        RPAREN,
        DOT,
        TERM,
    }

    private static final Map<Character, ReTokenType> ops = Map.of(
            '*', STAR,
            '+', PLUS,
            '?', HOOK,
            '&', AND,
            '|', OR,
            '(', LPAREN,
            ')', RPAREN,
            '.', DOT
    );

    public static ReToken opFromChar(char c) {
        var type = ops.get(c);
        if (type == null) {
            throw new UnkReTokenException(c + "");
        }

        return new ReToken(type, c + "");
    }

    @Override
    public String toString() {
        return "ReToken{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
