package syntactic;

import exceptions.SyntacticException;
import lexical.Scanner;
import lexical.Token;
import util.TokenType;

public class Parser {

    private Scanner scanner;
    private Token token;

    public Parser(Scanner scanner) throws Exception {
        this.scanner = scanner;
        // Pega o primeiro token para iniciar
        this.token = this.scanner.nextToken();
    }

    // ---------- Métodos de "casamento" (Match) e Verificação (Check) ----------

    /**
     * Verifica se o token atual corresponde ao texto esperado (para operadores +,-,*,/).
     * Se sim, consome o token e avança para o próximo.
     * Se não, lança uma exceção.
     */
    private void match(String expectedText) throws Exception {
        if (token != null && token.getText().equals(expectedText)) {
            token = scanner.nextToken();
        } else {
            String found = (token != null) ? "'" + token.getText() + "'" : "EOF (fim de arquivo)";
            throw new SyntacticException("Esperado '" + expectedText + "', mas foi encontrado " + found);
        }
    }

    /**
     * Verifica se o token atual corresponde ao tipo esperado (para keywords, IDENTIFIER, etc).
     * Se sim, consome o token e avança para o próximo.
     * Se não, lança uma exceção.
     */
    private void match(TokenType expectedType) throws Exception {
        if (token != null && token.getType() == expectedType) {
            token = scanner.nextToken();
        } else {
            String found = (token != null) ? token.getType().toString() : "EOF (fim de arquivo)";
            throw new SyntacticException("Esperado " + expectedType + ", mas foi encontrado " + found);
        }
    }

    /**
     * Apenas verifica o texto do token atual, sem consumir.
     */
    private boolean check(String text) {
        return token != null && token.getText().equals(text);
    }

    /**
     * Apenas verifica o tipo do token atual, sem consumir.
     */
    private boolean check(TokenType type) {
        return token != null && token.getType() == type;
    }


    // ---------- Métodos da Gramática (um para cada não-terminal) ----------

    /**
     * Ponto de entrada do parser.
     * programa : 'INICIO' 'DECLS' blocoDeclaracoes 'FIMDECLS' 'CODIGO' blocoComandos 'FIMPROG';
     */
    public void programa() throws Exception {
        match(TokenType.INICIO);
        match(TokenType.DECLS);
        blocoDeclaracoes();
        match(TokenType.FIMDECLS);
        match(TokenType.CODIGO);
        blocoComandos();
        match(TokenType.FIMPROG);

        // Se, após o FIMPROG, ainda houver tokens, é um erro.
        if (token != null) {
            throw new SyntacticException("Token inesperado '" + token.getText() + "' após o 'FIMPROG'.");
        }
    }

    /**
     * blocoDeclaracoes : declaracao blocoDeclaracoes | declaracao;
     * (Uma ou mais declarações)
     */
    public void blocoDeclaracoes() throws Exception {
        // FIRST(declaracao) = { IDENTIFIER }
        declaracao();

        if (check(TokenType.IDENTIFIER)) {
            blocoDeclaracoes();
        }
    }

    /**
     * declaracao : IDENTIFIER ':' tipo;
     */
    public void declaracao() throws Exception {
        match(TokenType.IDENTIFIER);
        match(TokenType.COLON);
        tipo();
    }

    /**
     * tipo : 'INT' | 'FLOAT';
     */
    public void tipo() throws Exception {
        if (check(TokenType.INT)) {
            match(TokenType.INT);
        } else if (check(TokenType.FLOAT)) {
            match(TokenType.FLOAT);
        } else {
            throw new SyntacticException("Esperado tipo 'INT' ou 'FLOAT', mas foi encontrado " + (token != null ? token.getText() : "EOF"));
        }
    }

    /**
     * expressaoAritmetica : termo expressaoAritmetica_linha;
     */
    public void expressaoAritmetica() throws Exception {
        termo();
        expressaoAritmetica_linha();
    }

    /**
     * expressaoAritmetica_linha : '+' termo expressaoAritmetica_linha | '-' termo expressaoAritmetica_linha | &;
     */
    public void expressaoAritmetica_linha() throws Exception {
        // Verificação por TEXTO aqui está correta, pois o scanner
        // agrupa todos como MATH_OPERATOR mas preserva o texto.
        if (check("+")) {
            match("+");
            termo();
            expressaoAritmetica_linha();
        } else if (check("-")) {
            match("-");
            termo();
            expressaoAritmetica_linha();
        }
        // else: Produção & (vazio). Não faz nada.
    }

    /**
     * termo : fator termo_linha;
     */
    public void termo() throws Exception {
        fator();
        termo_linha();
    }

    /**
     * termo_linha : '*' fator termo_linha | '/' fator termo_linha | &;
     */
    public void termo_linha() throws Exception {
        // Verificação por TEXTO
        if (check("*")) {
            match("*");
            fator();
            termo_linha();
        } else if (check("/")) {
            match("/");
            fator();
            termo_linha();
        }
        // else: Produção & (vazio). Não faz nada.
    }

    /**
     * fator : NUMINT | NUMREAL | IDENTIFIER | '(' expressaoAritmetica ')';
     */
    public void fator() throws Exception {
        // Corrigido para agrupar NUMINT e NUMREAL em NUMBER
        if (check(TokenType.NUMBER)) {
            match(TokenType.NUMBER);
        } else if (check(TokenType.IDENTIFIER)) {
            match(TokenType.IDENTIFIER);
        } else if (check(TokenType.L_PAREN)) { // Corrigido
            match(TokenType.L_PAREN);
            expressaoAritmetica();
            match(TokenType.R_PAREN); // Corrigido
        } else {
            throw new SyntacticException("Esperado NUMBER, IDENTIFIER ou '(', mas foi encontrado " + (token != null ? token.getText() : "EOF"));
        }
    }

    /**
     * expressaoRelacional : termoRelacional expressaoRelacional_linha;
     */
    public void expressaoRelacional() throws Exception {
        termoRelacional();
        expressaoRelacional_linha();
    }

    /**
     * expressaoRelacional_linha : operadorLogico termoRelacional expressaoRelacional_linha | &;
     */
    public void expressaoRelacional_linha() throws Exception {
        // FIRST(operadorLogico) = { 'E', 'OU' }
        if (check(TokenType.E) || check(TokenType.OU)) { // Corrigido
            operadorLogico();
            termoRelacional();
            expressaoRelacional_linha();
        }
        // else: Produção & (vazio). Não faz nada.
    }

    /**
     * termoRelacional : expressaoAritmetica OP_REL expressaoAritmetica | '(' expressaoRelacional ')';
     */
    public void termoRelacional() throws Exception {
        // Gramática ambígua para LL(1) no token '('.
        // Desambiguando:
        // Se for NUMBER ou IDENTIFIER, assume Regra 1.
        // Se for '(', assume Regra 2.
        // Veja a nota de aviso no início da resposta.

        if (check(TokenType.NUMBER) || check(TokenType.IDENTIFIER)) {
            // Regra 1: expressaoAritmetica OP_REL expressaoAritmetica
            expressaoAritmetica();
            match(TokenType.REL_OPERATOR); // Corrigido
            expressaoAritmetica();
        } else if (check(TokenType.L_PAREN)) {
            // Regra 2: '(' expressaoRelacional ')'
            match(TokenType.L_PAREN);
            expressaoRelacional();
            match(TokenType.R_PAREN);
        } else {
            throw new SyntacticException("Esperado expressão aritmética ou '(', mas foi encontrado " + (token != null ? token.getText() : "EOF"));
        }
    }

    /**
     * operadorLogico : 'E' | 'OU';
     */
    public void operadorLogico() throws Exception {
        if (check(TokenType.E)) {
            match(TokenType.E);
        } else if (check(TokenType.OU)) {
            match(TokenType.OU);
        } else {
            throw new SyntacticException("Esperado operador lógico 'E' ou 'OU', mas foi encontrado " + (token != null ? token.getText() : "EOF"));
        }
    }

    /**
     * blocoComandos : comando blocoComandos | comando;
     * (Um ou mais comandos)
     */
    public void blocoComandos() throws Exception {
        comando();

        // FIRST(comando) = { IDENTIFIER, LEIA, ESCREVA, SE, REPITA, BLOCO }
        if (check(TokenType.IDENTIFIER) || check(TokenType.LEIA) || check(TokenType.ESCREVA) ||
            check(TokenType.SE) || check(TokenType.REPITA) || check(TokenType.BLOCO)) {
            blocoComandos(); // Chamada recursiva
        }
    }

    /**
     * comando : atribuicao | entrada | saida | condicional | repeticao | subrotina;
     */
    public void comando() throws Exception {
        // Decidimos qual produção seguir com base no primeiro token (FIRST set)
        if (check(TokenType.IDENTIFIER)) {
            atribuicao();
        } else if (check(TokenType.LEIA)) {
            entrada();
        } else if (check(TokenType.ESCREVA)) {
            saida();
        } else if (check(TokenType.SE)) {
            condicional();
        } else if (check(TokenType.REPITA)) {
            repeticao();
        } else if (check(TokenType.BLOCO)) {
            subrotina();
        } else {
            throw new SyntacticException("Esperado um comando (IDENTIFIER, LEIA, ESCREVA, SE, REPITA, BLOCO), mas foi encontrado " + (token != null ? token.getText() : "EOF"));
        }
    }

    /**
     * atribuicao : IDENTIFIER '=' expressaoAritmetica;
     */
    public void atribuicao() throws Exception {
        match(TokenType.IDENTIFIER);
        match(TokenType.ASSIGNMENT); // Corrigido
        expressaoAritmetica();
    }

    /**
     * entrada : 'LEIA' IDENTIFIER;
     */
    public void entrada() throws Exception {
        match(TokenType.LEIA);
        match(TokenType.IDENTIFIER);
    }

    /**
     * saida : 'ESCREVA' '(' (IDENTIFIER | CADEIA) ')';
     */
    public void saida() throws Exception {
        match(TokenType.ESCREVA);
        match(TokenType.L_PAREN);

        // (IDENTIFIER | CADEIA)
        if (check(TokenType.IDENTIFIER)) {
            match(TokenType.IDENTIFIER);
        } else if (check(TokenType.CADEIA)) {
            match(TokenType.CADEIA);
        } else {
            throw new SyntacticException("Esperado IDENTIFIER ou CADEIA dentro do ESCREVA, mas foi encontrado " + (token != null ? token.getText() : "EOF"));
        }

        match(TokenType.R_PAREN);
    }

    /**
     * condicional : 'SE' expressaoRelacional 'ENTAO' comando | 'SE' expressaoRelacional 'ENTAO' comando 'SENAO' comando;
     * (Fatorado: 'SE' ... comando ( 'SENAO' comando | & ))
     */
    public void condicional() throws Exception {
        match(TokenType.SE);
        expressaoRelacional();
        match(TokenType.ENTAO);
        comando();

        // Parte opcional ( 'SENAO' comando | & )
        if (check(TokenType.SENAO)) {
            match(TokenType.SENAO);
            comando();
        }
    }

    /**
     * repeticao : 'REPITA' expressaoRelacional comando;
     */
    public void repeticao() throws Exception {
        match(TokenType.REPITA);
        expressaoRelacional();
        comando();
    }

    /**
     * subrotina : 'BLOCO' blocoComandos 'FIMBLOCO';
     */
    public void subrotina() throws Exception {
        match(TokenType.BLOCO);
        blocoComandos();
        match(TokenType.FIMBLOCO);
    }
}