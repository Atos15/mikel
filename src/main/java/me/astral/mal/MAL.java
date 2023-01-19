package me.astral.mal;

import me.astral.mal.model.MALProgram;
import me.astral.mal.token.MALTokenizer;

public class MAL {

    public static MALProgram parse(String code){
        MALTokenizer tokenizer = new MALTokenizer(code);
        MALParser parser = new MALParser(tokenizer.tokenize());
        return parser.parse();
    }

}
