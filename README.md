# mini_compiler  

## Descrição  
O **mini_compiler** é um projeto acadêmico em **Java** voltado para a disciplina de **Construção de Compiladores I**.  
Este módulo corresponde ao **Checkpoint 01: Analisador Léxico** e tem como objetivo implementar e estender um analisador léxico simples para reconhecer identificadores, números, operadores, palavras reservadas, parênteses e comentários, além de tratar erros léxicos.  

O projeto é parte de um compilador em desenvolvimento incremental e **possui peso 2 na primeira nota da disciplina**.  

---

## Estrutura do Projeto  

```mini_compiler/
│
├── src/
│ ├── lexical/
│ │ ├── Scanner.java # Implementação do analisador léxico
│ │ └── Token.java # Estrutura de dados para representar tokens
│ │
│ ├── mini_compiler/
│ │ └── Main.java # Classe principal para execução do compilador
│ │
│ ├── util/
│ │ ├── TokenType.java # Enumeração com os tipos de tokens reconhecidos
│ │ └── ReservedWords.java # Tabela com as palavras reservadas
│ └── programa.mc # arquivo para teste
│
└── README.md # Documentação do projeto
```

---

## Requisitos  

- **Java 11+** (recomendado: OpenJDK 17 ou superior)  
- IDE ou editor de texto de sua preferência (IntelliJ, VS Code, Eclipse, etc.)  

---

## Compilação e Execução  

Dentro da pasta `src`, compile os arquivos:  

```
javac mini_compiler/Main.java
```

E execute o programa:

```
java mini_compiler.Main
```

O arquivo de entrada pode ser configurado dentro da classe Main.java ou passado como argumento (dependendo da sua implementação).


## Exemplos de Uso

Entrada de teste (programa.mc):

```c
int x = 10;
float y = 3.14;
/* comentário de múltiplas linhas */
if (x >= y) {
    print(x);
}
```

## Saída esperada (tokens):

```
[INT, IDENT(x), ASSIGN, NUMBER(10), SEMICOLON]
[FLOAT, IDENT(y), ASSIGN, NUMBER(3.14), SEMICOLON]
[IF, LPAREN, IDENT(x), GTE, IDENT(y), RPAREN, LBRACE]
[PRINT, LPAREN, IDENT(x), RPAREN, SEMICOLON]
[RBRACE]
```
## Contribuição

Este projeto é desenvolvido no contexto da disciplina. Sugestões e melhorias podem ser feitas via Merge Requests ou discutidas em sala de aula.

## Licença

Uso acadêmico restrito à disciplina de Construção de Compiladores I.

## Status

Atualmente em desenvolvimento no módulo de Analisador Léxico. Próximos módulos incluirão Analisador Sintático e Analisador Semântico.
