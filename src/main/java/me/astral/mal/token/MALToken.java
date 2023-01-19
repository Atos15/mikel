package me.astral.mal.token;

public record MALToken(Type type, String value) {
    public enum Type{
        SEPARATOR,
        BEGIN_DIRECTIVE,
        NUMERIC,
        LEFT_PAREN,
        RIGHT_PAREN,
        KEYWORD,
        OPERATOR,
        WORD,
        EOL
    }

}
