package me.astral.mal.token;

import java.util.ArrayList;
import java.util.List;

public class MALTokenizer {

    private final String code;
    private int index;
    private boolean codeLine = false;

    public MALTokenizer(String code){
        this.code = code;
        this.index = 0;
    }

    public List<MALToken> tokenize(){
        List<MALToken> tokens = new ArrayList<>();
        MALToken token;
        while ((token = nextToken()) != null){
            tokens.add(token);
        }
        return tokens;
    }

    private boolean skipWhitespace(){
        if (done())
            return false;

        boolean skipped = false;
        while(!done() && Character.isWhitespace(current())){
            if (codeLine && current() == '\n')
                break;
            nextIndex();
            skipped = true;
        };
        return skipped;
    }

    private boolean skipComments(){
        if (done())
            return false;

        char curr = current();
        if (curr != '/')
            return false;

        char next = next();
        if (next != '/')
            throw new IllegalStateException("Expected / after /");

        boolean skipped = false;
        while(!done() && current() != '\n'){
            skipped = true;
            nextIndex();
        }
        return  skipped;
    }

    private MALToken nextToken(){
        while (skipWhitespace() || skipComments()){}

        if (done())
            return null;

        char currentChar = advance();

        codeLine = currentChar != '\n';

        return switch (currentChar){
            case '\n' -> new MALToken(MALToken.Type.EOL, "\n");
            case ';' -> new MALToken(MALToken.Type.SEPARATOR, ";");
            case '(' -> new MALToken(MALToken.Type.LEFT_PAREN, "(");
            case ')' -> new MALToken(MALToken.Type.RIGHT_PAREN, ")");
            case '+' -> new MALToken(MALToken.Type.OPERATOR, "+");
            case '-' -> new MALToken(MALToken.Type.OPERATOR, "-");
            case '>' -> {
                char next = advance();
                if (next != '>')
                    throw new IllegalStateException("Expected > after >");
                yield new MALToken(MALToken.Type.OPERATOR, ">>");
            }
            case '=' -> new MALToken(MALToken.Type.OPERATOR, "=");
            case '<' -> {
                char next = advance();
                if (next != '<')
                    throw new IllegalStateException("Expected < after <");
                yield new MALToken(MALToken.Type.OPERATOR, "<<");
            }
            case '1' -> new MALToken(MALToken.Type.NUMERIC, "1");
            case '8' -> new MALToken(MALToken.Type.NUMERIC, "8");
            case '0' -> {
                if (!hasNext())
                    yield new MALToken(MALToken.Type.NUMERIC, "0");
                char curr = current();
                if (curr == 'x'){
                    advance();
                    StringBuilder builder = new StringBuilder("0x");
                    while (!done() && isHexadecimalChar(current())){
                        builder.append(current());
                        nextIndex();
                    }
                    yield new MALToken(MALToken.Type.NUMERIC, builder.toString());
                }else{
                    yield new MALToken(MALToken.Type.NUMERIC, "0");
                }
            }
            default -> {
                StringBuilder builder = new StringBuilder(Character.toString(currentChar));
                while(!done() && (isValidTextCharacter(current()))){
                    builder.append(current());
                    nextIndex();
                }
                String value = builder.toString();

                yield switch (value) {
                    case "if", "goto", "else" -> new MALToken(MALToken.Type.KEYWORD, value);
                    default -> new MALToken(value.startsWith(".") ? MALToken.Type.BEGIN_DIRECTIVE : MALToken.Type.WORD, value);
                };
            }
        };

    }

    private boolean isValidTextCharacter(char c){
        return Character.isAlphabetic(c) || Character.isDigit(c) || "_".contains(Character.toString(c));
    }

    private boolean hasNext(){
        return index < code.length() - 1;
    }

    private void nextIndex(){
        index++;
    }

    private char advance(){
        return code.charAt(index++);
    }

    private char next(){
        return code.charAt(++index);
    }

    private char current(){
        return code.charAt(index);
    }

    private boolean done(){
        return index >= code.length();
    }

    private char peek(){
        return code.charAt(index + 1);
    }

    private boolean isHexadecimalChar(char c){
        return  (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F')
                || (c >= '0' && c <= '9');
    }
}
