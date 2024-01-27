package org.example;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.Tokens.*;

public class Lexer {
    public final StreamTokenizer stream;

    public static final List<Integer> S = new ArrayList<>();
    public static final Map<String, Integer> identifiers = new HashMap<>();
    public static int idx = 0;

    public Lexer() throws FileNotFoundException {
        stream = new StreamTokenizer(new FileReader("F:\\pdup\\src\\main\\java\\org\\example\\Test.java"));
    }

    public void parse() throws IOException {
        for (int t = stream.nextToken(); t != StreamTokenizer.TT_EOF; t = stream.nextToken()) {
            switch (t) {
                case StreamTokenizer.TT_WORD: {
                    Tokens tok;
                    switch (stream.sval) {
                        case "package": {
                            tok = PACKAGE;
                            break;
                        }
                        case "public": {
                            tok = PUBLIC;
                            break;
                        }
                        case "class": {
                            tok = CLASS;
                            break;
                        }
                        case "int": {
                            tok = INT;
                            break;
                        }
                        case "if": {
                            tok = IF;
                            break;
                        }
                        case "return": {
                            tok = RETURN;
                            break;
                        }
                        default: {
                            tok = IDENTIFIER;
                        }
                    }
                    if (tok != IDENTIFIER) {
                        S.add(-tok.ordinal());
                    } else {
                        S.add(identifiers.computeIfAbsent(stream.sval, k -> idx++));
                    }
                    break;
                }
                case StreamTokenizer.TT_NUMBER: {
                    System.out.println(t);
                    break;
                }
                default: {
                    Tokens tok;
                    switch (t) {
                        case '(': {
                            tok = LPAR;
                            break;
                        }
                        case ')': {
                            tok = RPAR;
                            break;
                        }
                        case '{': {
                            tok = LBRACE;
                            break;
                        }
                        case '}': {
                            tok = RBRACE;
                            break;
                        }
                        case ';': {
                            tok = SEMICOLON;
                            break;
                        }
                        case '>': {
                            tok = GREATER;
                            break;
                        }
                        case ',': {
                            tok = COMMA;
                            break;
                        }
                        default: {
                            throw new RuntimeException();
                        }
                    }
                    S.add(-tok.ordinal());
                    break;
                }
            }
        }
        S.add(-EOF.ordinal());
    }

    public static void main(String[] args) throws Exception {
        var l = new Lexer();
        l.parse();
        System.out.println(Lexer.S);
    }
}
