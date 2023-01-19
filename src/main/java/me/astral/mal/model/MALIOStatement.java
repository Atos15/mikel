package me.astral.mal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MALIOStatement {
    READ("rd"),
    WRITE("wr");

    private final String keyword;

    public static MALIOStatement fromKeyword(String keyword){
        for (MALIOStatement statement : values())
            if (statement.getKeyword().equals(keyword))
                return statement;
        return null;
    }
}
