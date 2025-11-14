package lexical;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import util.ReservedWords;
import util.TokenType;

public class Scanner {
	private int state;
	private char[] sourceCode;
	private int pos, lin, col;

	public Scanner(String filename) {
		try {
			String content = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
			sourceCode = content.toCharArray();
			pos = 0;
			col = 0;
			lin = 1;     
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Token nextToken() {
		char currentChar;
		String content = "";
		state = 0;

		while (true) {
			
			if (isEoF()) {
				
				switch(state) {
					case 0:
						return null;
					case 1:
						TokenType type = ReservedWords.TABLE.getOrDefault(content, TokenType.IDENTIFIER);
						return new Token(type, content);
					case 5: 
					case 6: 
						return new Token(TokenType.NUMBER, content);
					case 2:
						return new Token(TokenType.ASSIGNMENT, content);
					case 3:
						if (content.equals("!")) {
							erro_caracter('!');
						}
						return new Token(TokenType.REL_OPERATOR, content);
					case 4:
						erro_cadeia(content);
					case 7:
						return new Token(TokenType.MATH_OPERATOR, content);
					case 8: 
						throw new RuntimeException("Erro léxico: Comentário em bloco não fechado (EOF)");
					default:
						return null; 
				}
			}
			currentChar = nextChar();

			switch (state) {
				case 0:
					if (isLetter(currentChar)) {
						content += currentChar;
						state = 1;
					} else if (isSlash(currentChar)) {
						content += currentChar;
						state = 7;
					} else if (isMathOperator(currentChar)) {
						content += currentChar;
						return new Token(TokenType.MATH_OPERATOR, content);
					} else if (currentChar == ':') {
						content += currentChar;
						return new Token(TokenType.COLON, content);
					} else if (isAssignOperator(currentChar)) {
						content += currentChar;
						state = 2;
					} else if (isRelOperator(currentChar)) {
						content += currentChar;
						state = 3;
					} else if (currentChar == '(') {
						content += currentChar;
						return new Token(TokenType.L_PAREN, content);
					} else if (currentChar == ')') {
						content += currentChar;
						return new Token(TokenType.R_PAREN, content);
					} else if (isPoint(currentChar)) {
						content += currentChar;
						state = 4;
					} else if (isDigit(currentChar)) {
						content += currentChar;
						state = 6;
					} else if (isHash(currentChar)) {
						while (!isEoF() && currentChar != '\n') {
							currentChar = nextChar();
						}
					} else if (!Character.isWhitespace(currentChar)) {
						erro_caracter(currentChar);				
					}
					break;
				case 1:
					if (isLetter(currentChar) || isDigit(currentChar)) {
						content += currentChar;
					} else {
						back();
						TokenType type = ReservedWords.TABLE.getOrDefault(content, TokenType.IDENTIFIER);
						return new Token(type, content);
					}
					break;
				case 2:
					if (isAssignOperator(currentChar)) {
						content += currentChar;
						return new Token(TokenType.REL_OPERATOR, content);
					} else {
						back();
						return new Token(TokenType.ASSIGNMENT, content);
					}
				case 3:
					if (isAssignOperator(currentChar)) {
						content += currentChar;
						return new Token(TokenType.REL_OPERATOR, content);
					} else if (!content.equals("!")) {
						back();
						return new Token(TokenType.REL_OPERATOR, content);
					} else {
						back();
						erro_caracter(sourceCode[pos-1]);
					}
					
				case 4:
					if (isDigit(currentChar)) {
						content += currentChar;
						state = 5;
					} else if (content.length() <= 1){
						back();
						erro_caracter(sourceCode[pos-1]);
					} else {
						back();
						erro_cadeia(content);
					}
					break;
				case 5:
					if (isDigit(currentChar)) {
						content += currentChar;
					} else if(!isLetter(currentChar)){
						back();
						return new Token(TokenType.NUMBER, content);
					} else {
						while (isLetter(currentChar) || isDigit(currentChar)) {
							content += currentChar;
							currentChar = nextChar();
						}
						back();
						erro_cadeia(content);
					}
					break;
				case 6:
					if (isDigit(currentChar)) {
						content += currentChar;
					} else if (isPoint(currentChar)) {
						content += currentChar;
						state = 4;
					} else if(!isLetter(currentChar)){
						back();
						return new Token(TokenType.NUMBER, content);
					} else {
						while (isLetter(currentChar) || isDigit(currentChar)) {
							content += currentChar;
							currentChar = nextChar();
						}
						back();
						erro_cadeia(content);
					}
					break;
				case 7:
					if (isAsterisk(currentChar)) {
						content = "";
						state = 8;
					} else {
						back();
						return new Token(TokenType.MATH_OPERATOR, content);
					}
					break;
				case 8:
					while (!isEoF()) {
						currentChar = nextChar();

						if (isAsterisk(currentChar)) {
							if (!isEoF()) {
								char next = nextChar();

								if (isSlash(next)) {
									state = 0;
									break;
								}
							}
						}
					}
					state = 0;
					break;
			}
		}
	}

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isMathOperator(char c) {
		return c == '+' || c == '-' || c == '*' || c == '/';
	}

	private boolean isRelOperator(char c) {
		return c == '>' || c == '<' || c == '=' || c == '!';
	}

	private char nextChar() {
		if (sourceCode[pos] == '\n') {
			lin++;
			col = 0;
		} else {
			col++;
		}
		return sourceCode[pos++];
	}

	private void erro_caracter(char c){
		throw new RuntimeException(
			"Erro léxico na linha " + lin + ", coluna " + col + ". Símbolo '" + c + "' não reconhecido."
			);
	}
	
	private void erro_cadeia(String s){
		throw new RuntimeException(
			"Erro léxico na linha " + lin + ", coluna " + (col-s.length()+1) + " à " + col + ". Cadeia '" + s + "' não reconhecida."
			);
	}
	
	private void back() {
		col--;
		pos--;
	}

	private boolean isEoF() {
		return pos >= sourceCode.length;
	}

	private boolean isPoint(char c) {
		return c == '.';
	}

	private boolean isAssignOperator(char c) {
		return c == '=';
	}

	private boolean isSlash(char c) {
		return c == '/';
	}

	private boolean isHash(char c) {
		return c == '#';
	}

	private boolean isAsterisk(char c) {
		return c == '*';
	}
}
