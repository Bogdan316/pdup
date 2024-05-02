package upt.baker.pdup.regex.parser;

public class UnkReTokenException extends RuntimeException {
    public UnkReTokenException(String t) {
        super("Unknown token: '" + t + "'");
    }
}
