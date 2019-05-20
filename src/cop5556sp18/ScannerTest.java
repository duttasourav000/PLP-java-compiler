 /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(pos, t.pos);
		assertEquals(length, t.length);
		assertEquals(line, t.line());
		assertEquals(pos_in_line, t.posInLine());
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}
	


	/**
	 * Simple test case with an empty program.  The only Token will be the EOF Token.
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, the end of line 
	 * character would be inserted by the text editor.
	 * Showing the input will let you check your input is 
	 * what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	

	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failIllegalChar() throws LexicalException {
		String input = ";\n;~";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(3,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}




	@Test
	public void testParens() throws LexicalException {
		String input = "(\n)";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 2, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	

	@Test
	public void testOperators() throws LexicalException {
		String input = "* ** * \n***** < > >=<= ?! :=: == !=&+-/@%|";
		// "==="
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_TIMES, 0, 1, 1, 1);
		checkNext(scanner, OP_POWER, 2, 2, 1, 3);
		checkNext(scanner, OP_TIMES, 5, 1, 1, 6);
		checkNext(scanner, OP_POWER, 8, 2, 2, 1);
		checkNext(scanner, OP_POWER, 10, 2, 2, 3);
		checkNext(scanner, OP_TIMES, 12, 1, 2, 5);
		checkNext(scanner, OP_LT, 14, 1, 2, 7);
		checkNext(scanner, OP_GT, 16, 1, 2, 9);
		checkNext(scanner, OP_GE, 18, 2, 2, 11);
		checkNext(scanner, OP_LE, 20, 2, 2, 13);
		checkNext(scanner, OP_QUESTION, 23, 1, 2, 16);
		checkNext(scanner, OP_EXCLAMATION, 24, 1, 2, 17);
		checkNext(scanner, OP_ASSIGN, 26, 2, 2, 19);
		checkNext(scanner, OP_COLON, 28, 1, 2, 21);
		checkNext(scanner, OP_EQ, 30, 2, 2, 23);
		checkNext(scanner, OP_NEQ, 33, 2, 2, 26);
		checkNext(scanner, OP_AND, 35, 1, 2, 28);
		checkNext(scanner, OP_PLUS, 36, 1, 2, 29);
		checkNext(scanner, OP_MINUS, 37, 1, 2, 30);
		checkNext(scanner, OP_DIV, 38, 1, 2, 31);
		checkNext(scanner, OP_AT, 39, 1, 2, 32);
		checkNext(scanner, OP_MOD, 40, 1, 2, 33);
		checkNext(scanner, OP_OR, 41, 1, 2, 34);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSeparators() throws LexicalException {
		String input = ")<<(}{>;]\n>>>,[\n< .";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, RPAREN, 0, 1, 1, 1);
		checkNext(scanner, LPIXEL, 1, 2, 1, 2);
		checkNext(scanner, LPAREN, 3, 1, 1, 4);
		checkNext(scanner, RBRACE, 4, 1, 1, 5);
		checkNext(scanner, LBRACE, 5, 1, 1, 6);
		checkNext(scanner, OP_GT, 6, 1, 1, 7);
		checkNext(scanner, SEMI, 7, 1, 1, 8);
		checkNext(scanner, RSQUARE, 8, 1, 1, 9);
		checkNext(scanner, RPIXEL, 10, 2, 2, 1);
		checkNext(scanner, OP_GT, 12, 1, 2, 3);
		checkNext(scanner, COMMA, 13, 1, 2, 4);
		checkNext(scanner, LSQUARE, 14, 1, 2, 5);
		checkNext(scanner, OP_LT, 16, 1, 3, 1);
		checkNext(scanner, DOT, 18, 1, 3, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testBoolean() throws LexicalException {
		String input = "truefalse false true";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 9, 1, 1);
		checkNext(scanner, BOOLEAN_LITERAL, 10, 5, 1, 11);
		checkNext(scanner, BOOLEAN_LITERAL, 16, 4, 1, 17);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testInteger() throws LexicalException {
		String input = "123 45603 00 0";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 4, 5, 1, 5);
		checkNext(scanner, INTEGER_LITERAL, 10, 1, 1, 11);
		checkNext(scanner, INTEGER_LITERAL, 11, 1, 1, 12);
		checkNext(scanner, INTEGER_LITERAL, 13, 1, 1, 14);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testFlotingPoint() throws LexicalException {
		String input = " .09800 0.34 000. .0 .000 6.7 000678 0008. 0000922.00080000";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 1, 6, 1, 2);
		checkNext(scanner, FLOAT_LITERAL, 8, 4, 1, 9);
		checkNext(scanner, INTEGER_LITERAL, 13, 1, 1, 14);
		checkNext(scanner, INTEGER_LITERAL, 14, 1, 1, 15);
		checkNext(scanner, FLOAT_LITERAL, 15, 2, 1, 16);
		checkNext(scanner, FLOAT_LITERAL, 18, 2, 1, 19);
		checkNext(scanner, FLOAT_LITERAL, 21, 4, 1, 22);
		checkNext(scanner, FLOAT_LITERAL, 26, 3, 1, 27);
		checkNext(scanner, INTEGER_LITERAL, 30, 1, 1, 31);
		checkNext(scanner, INTEGER_LITERAL, 31, 1, 1, 32);
		checkNext(scanner, INTEGER_LITERAL, 32, 1, 1, 33);
		checkNext(scanner, INTEGER_LITERAL, 33, 3, 1, 34);
		checkNext(scanner, INTEGER_LITERAL, 37, 1, 1, 38);
		checkNext(scanner, INTEGER_LITERAL, 38, 1, 1, 39);
		checkNext(scanner, INTEGER_LITERAL, 39, 1, 1, 40);
		checkNext(scanner, FLOAT_LITERAL, 40, 2, 1, 41);
		checkNext(scanner, INTEGER_LITERAL, 43, 1, 1, 44);
		checkNext(scanner, INTEGER_LITERAL, 44, 1, 1, 45);
		checkNext(scanner, INTEGER_LITERAL, 45, 1, 1, 46);
		checkNext(scanner, INTEGER_LITERAL, 46, 1, 1, 47);
		checkNext(scanner, FLOAT_LITERAL, 47, 12, 1, 48);
		checkNextIsEOF(scanner);
	}
	

	@Test
	public void testFlotingPointException() throws LexicalException {
		String input = "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999.";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {
			show(e);
			assertEquals(461,e.getPos());
			throw e;
		}
	}
	
	
	@Test
	public void testIntegerException() throws LexicalException {
		String input = "2147483648";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {
			show(e);
			assertEquals(10,e.getPos());
			throw e;
		}
	}
	
	
	@Test
	public void testKeyword() throws LexicalException {
		String input = "write Z Zen work show peice red cart_x";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_write, 0, 5, 1, 1);
		checkNext(scanner, KW_Z, 6, 1, 1, 7);
		checkNext(scanner, IDENTIFIER, 8, 3, 1, 9);
		checkNext(scanner, IDENTIFIER, 12, 4, 1, 13);
		checkNext(scanner, KW_show, 17, 4, 1, 18);
		checkNext(scanner, IDENTIFIER, 22, 5, 1, 23);
		checkNext(scanner, KW_red, 28, 3, 1, 29);
		checkNext(scanner, KW_cart_x, 32, 6, 1, 33);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testComment1() throws LexicalException {
		String input = "/*write Z Zen work show peice red*/cart_x/*This is / a comment/* This is too**/ heavy.sleep";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_cart_x, 35, 6, 1, 36);
		checkNext(scanner, IDENTIFIER, 80, 5, 1, 81);
		checkNext(scanner, DOT, 85, 1, 1, 86);
		checkNext(scanner, KW_sleep, 86, 5, 1, 87);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testComment2() throws LexicalException {
		String input = "/**/";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testComment3() throws LexicalException {
		String input = "/*\n\n\n\n\n*/\n\n\n*/";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_TIMES, 12, 1, 9, 1);
		checkNext(scanner, OP_DIV, 13, 1, 9, 2);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void failIllegalCharEquals() throws LexicalException {
		String input = "== := =";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {
			show(e);
			assertEquals(7,e.getPos());
			throw e;
		}
	}
	
	
	@Test
	public void testHW1_testkw7() throws LexicalException {
		String input = "abs";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_abs, 0, 3, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testHW1_testBoolean() throws LexicalException {
		String input = "true false true0 0false";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, BOOLEAN_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, BOOLEAN_LITERAL, 5, 5, 1, 6);
		checkNext(scanner, IDENTIFIER, 11, 5, 1, 12);
		checkNext(scanner, INTEGER_LITERAL, 17, 1, 1, 18);
		checkNext(scanner, BOOLEAN_LITERAL, 18, 5, 1, 19);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testHW1_testIdent2() throws LexicalException {
		String input = "i1 j2j ";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 2, 1, 1);
		checkNext(scanner, IDENTIFIER, 3, 3, 1, 4);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testHW1_testIdent6() throws LexicalException {
		String input = "i$ i$$ i$j i$_1";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 2, 1, 1);
		checkNext(scanner, IDENTIFIER, 3, 3, 1, 4);
		checkNext(scanner, IDENTIFIER, 7, 3, 1, 8);
		checkNext(scanner, IDENTIFIER, 11, 4, 1, 12);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testHW1_testIntLiterals6() throws LexicalException {
		String input = "010a010a 1\n1";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 2, 1, 2);
		checkNext(scanner, IDENTIFIER, 3, 5, 1, 4);
		checkNext(scanner, INTEGER_LITERAL, 9, 1, 1, 10);
		checkNext(scanner, INTEGER_LITERAL, 11, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void dummyTest() throws LexicalException {
		String input = "-25";
		//String input = "010a010a 1\\n1";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
	}
}
	

