/*
Alunos:
Davi Vinícius chaves de Lima olimpio
Letícia Beatriz Machado dos Anjos
 */

package mini_compiler;

import exceptions.SyntacticException;
import lexical.Scanner;
import syntactic.Parser;

public class Main {

	public static void main(String[] args) {
		Scanner sc = new Scanner("C:\\Users\\lbeat\\Documents\\mini compilador\\mini-compilador\\programa.mc");
		try {
			Parser parser = new Parser(sc);
			parser.programa();
			System.out.println("Compilation successful");
		} catch (SyntacticException e) {
			System.out.println("Syntactic error: " + e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
