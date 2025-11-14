package util;

import java.util.Map;
import java.util.HashMap;

public class ReservedWords {
    public static final Map<String, TokenType> TABLE = new HashMap<>();

    static {
        TABLE.put("INT", TokenType.INT);
        TABLE.put("FLOAT", TokenType.FLOAT);
        TABLE.put("INICIO", TokenType.INICIO);
        TABLE.put("DECLS", TokenType.DECLS);
        TABLE.put("FIMDECLS", TokenType.FIMDECLS);
        TABLE.put("CODIGO", TokenType.CODIGO);
        TABLE.put("FIMPROG", TokenType.FIMPROG);
        TABLE.put("LEIA", TokenType.LEIA);
        TABLE.put("ESCREVA", TokenType.ESCREVA);
        TABLE.put("SE", TokenType.SE);
        TABLE.put("ENTAO", TokenType.ENTAO);
        TABLE.put("SENAO", TokenType.SENAO);
        TABLE.put("REPITA", TokenType.REPITA);
        TABLE.put("BLOCO", TokenType.BLOCO);
        TABLE.put("FIMBLOCO", TokenType.FIMBLOCO);
        TABLE.put("E", TokenType.E);
        TABLE.put("OU", TokenType.OU);
    }
}
