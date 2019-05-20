package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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

import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

public class Parser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	/*public void program() throws SyntaxException {
		match(IDENTIFIER);
		block();
	}*/
	
	Program program() throws SyntaxException {
		Token first = t;
		Token progName = match(IDENTIFIER);
		Block block = block();
		return new Program (first, progName, block);
	}

	/*
	 * Block ::= { ( (Declaration | Statement) ; )* }
	 */

	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = { KW_input, KW_write, IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha, KW_while, KW_if,
			KW_show, KW_sleep };
	Kind[] firstAssignment = { IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha };
	Kind[] firstOrExpression = { OP_PLUS, OP_MINUS,  OP_EXCLAMATION, INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN, KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_blue, KW_green, KW_alpha, IDENTIFIER, KW_Z, KW_default_height, KW_default_width, LPIXEL };
	Kind[] firstFunctionName = { KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_blue, KW_green, KW_alpha };
	Kind[] firstPredefinedName = { KW_Z, KW_default_height, KW_default_width };
	public Block block() throws SyntaxException {
		Token first = t;
		match(LBRACE);
		List<ASTNode> decsOrStatements = new ArrayList<ASTNode>();
		while (isKind(firstDec) | isKind(firstStatement)) {
			if (isKind(firstDec)) {
				decsOrStatements.add(declaration());
			}
			else if (isKind(firstStatement)) {
				decsOrStatements.add(statement());
			}
			match(SEMI);
		}

		match(RBRACE);
		return new Block(first, decsOrStatements);
	}

	public Declaration declaration() throws SyntaxException {
		Token first = t;
		Token type = null;
		Token name = null;
		Expression width = null;
		Expression height = null;
		type = t;
		if (isKind(KW_image)) {
			match(KW_image);
			name = t;
			match(IDENTIFIER);
			if (isKind(LSQUARE)) {
				match(LSQUARE);
				width = expression();
				match(COMMA);
				height = expression();
				match(RSQUARE);
			}
		}
		else if (isKind(KW_int)) {
			match(KW_int);
			name = t;
			match(IDENTIFIER);
		}
		else if (isKind(KW_float)) {
			type = t;
			match(KW_float);
			name = t;
			match(IDENTIFIER);
		}
		else if (isKind(KW_boolean)) {
			match(KW_boolean);
			name = t;
			match(IDENTIFIER);
		}
		else {
			match(KW_filename);
			name = t;
			match(IDENTIFIER);
		}
		
		return new Declaration(first, type, name, width, height);
	}
	
	public Statement statement() throws SyntaxException {
		Statement s = null;
		if (isKind(KW_input)) {
			s = statementInput();
		} else if (isKind(KW_write)) {
			s = statementWrite();
		} else if (isKind(firstAssignment)) {
			s = statementAssignment();
		} else if (isKind(KW_while)) {
			s = statementWhile();
		} else if (isKind(KW_if)) {
			s = statementIf();
		} else if (isKind(KW_show)) {
			s = statementShow();
		} else {
			s = statementSleep();
		}
		
		return s;
	}
	
	public Expression expression() throws SyntaxException {
		Token first = t;
		Expression e0 = orExpression();
		if (isKind(OP_QUESTION)) {
			match(OP_QUESTION);
			Expression e1 = expression();
			match(OP_COLON);
			Expression e2 = expression();
			e0 = new ExpressionConditional(first, e0, e1, e2);
		}
		
		return e0;
	}
	
	public Expression orExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = andExpression();
		while (isKind(OP_OR)) {
			Token op = t;
			match(OP_OR);
			Expression e1 = andExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression andExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = eqExpression();
		while (isKind(OP_AND)) {
			Token op = t;
			match(OP_AND);
			Expression e1 = eqExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression eqExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = relExpression();
		while (isKind(OP_EQ) || isKind(OP_NEQ)) {
			Token op = t;
			if (isKind(OP_EQ)) {
				match(OP_EQ);
			}
			else {
				match(OP_NEQ);
			}
			
			Expression e1 = relExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression relExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = addExpression();
		while (isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
			Token op = t;
			if (isKind(OP_LT)) {
				match(OP_LT);
			} else if (isKind(OP_GT)) {
				match(OP_GT);
			} else if (isKind(OP_LE)) {
				match(OP_LE);
			} else {
				match(OP_GE);
			}
			
			Expression e1 = addExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression addExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = multExpression();
		while (isKind(OP_PLUS) || isKind(OP_MINUS)) {
			Token op = t;
			if (isKind(OP_PLUS)) {
				match(OP_PLUS);
			}
			else {
				match(OP_MINUS);
			}
			
			Expression e1 = multExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression multExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = powerExpression();
		while (isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
			Token op = t;
			if (isKind(OP_TIMES)) {
				match(OP_TIMES);
			}
			else if (isKind(OP_DIV)) {
				match(OP_DIV);
			} else {
				match(OP_MOD);
			}
			
			Expression e1 = powerExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression powerExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = unaryExpression();
		if (isKind(OP_POWER)) {
			Token op = t;
			match(OP_POWER);
			Expression e1 = powerExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		
		return e0;
	}
	
	public Expression unaryExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = null;
		if (isKind(OP_PLUS) || isKind(OP_MINUS)) {
			Token op = t;
			if (isKind(OP_PLUS)) {
				match(OP_PLUS);
				e0 = unaryExpression();
				e0 = new ExpressionUnary(first, op, e0);
			}
			else {
				match(OP_MINUS);
				e0 = unaryExpression();
				e0 = new ExpressionUnary(first, op, e0);
			}
		}
		else {
			e0 = unaryEexpressionNotPlusMinus();
		}
		
		return e0;
	}
	
	public Expression unaryEexpressionNotPlusMinus() throws SyntaxException {
		Token first = t;
		Token op = null;
		Expression e0 = null;
		if (isKind(OP_EXCLAMATION)) {
			op = t;
			match(OP_EXCLAMATION);
			e0 = unaryExpression();
			e0 = new ExpressionUnary(first, op, e0);
		}
		else {
			e0 = primary();
		}
		
		return e0;
	}
	
	public Expression primary() throws SyntaxException {
		Token first = t;
		Expression e0 = null;
		if (isKind(INTEGER_LITERAL)) {
			e0 = new ExpressionIntegerLiteral(first, t);
			match(INTEGER_LITERAL);
		} else if (isKind(BOOLEAN_LITERAL)) {
			e0 = new ExpressionBooleanLiteral(first, t);
			match(BOOLEAN_LITERAL);
		} else if (isKind(FLOAT_LITERAL)) {
			e0 = new ExpressionFloatLiteral(first, t);
			match(FLOAT_LITERAL);
		} else if (isKind(LPAREN)) {
			match(LPAREN);
			e0 = expression();
			match(RPAREN);
		} else if (isKind(firstFunctionName)) {
			e0 = functionApplication();
		} else if (isKind(IDENTIFIER)) {
			e0 = new ExpressionIdent(first, t);
			Token name = t;
			match(IDENTIFIER);
			if (isKind(LSQUARE)) {
				PixelSelector p = pixelSelector();
				e0 = new ExpressionPixel(first, name, p);
			}
		} else if (isKind(firstPredefinedName)) {
			e0 = predefinedName();
		} else {
			e0 = pixelConstructor();
		}
		
		return e0;
	}
	
	public ExpressionPixelConstructor pixelConstructor() throws SyntaxException {
		Token first = t;
		Expression e0 = null;
		Expression e1 = null;
		Expression e2 = null;
		Expression e3 = null;
		match(LPIXEL);
		e0 = expression();
		match(COMMA);
		e1 = expression();
		match(COMMA);
		e2 = expression();
		match(COMMA);
		e3 = expression();
		match(RPIXEL);
		return new ExpressionPixelConstructor(first, e0, e1, e2, e3);
	}
	
	public ExpressionPixel pixelExpression() throws SyntaxException {
		Token first = t;
		Token name = t;
		match(IDENTIFIER);
		PixelSelector p = pixelSelector();
		return new ExpressionPixel(first, name, p);
	}
	
	public Expression functionApplication() throws SyntaxException {
		Token first = t;
		Expression e0 = null;
		Token function = functionName();
		if (isKind(LPAREN)) {
			match(LPAREN);
			Expression e1 = expression();
			match(RPAREN);
			e0 = new ExpressionFunctionAppWithExpressionArg(first, function, e1);
		}
		else {
			match(LSQUARE);
			Expression e1 = expression();
			match(COMMA);
			Expression e2 = expression();
			match(RSQUARE);
			e0 = new ExpressionFunctionAppWithPixel(first, function, e1, e2);
		}
		
		return e0;
	}
	
	public Token functionName() throws SyntaxException {
		Token name = t;
		switch (this.t.kind) {
			case KW_sin:
				match(KW_sin);
				break;
			case KW_cos:
				match(KW_cos);
				break;
			case KW_atan:
				match(KW_atan);
				break;
			case KW_abs:
				match(KW_abs);
				break;
			case KW_log:
				match(KW_log);
				break;
			case KW_cart_x:
				match(KW_cart_x);
				break;
			case KW_cart_y:
				match(KW_cart_y);
				break;
			case KW_polar_a:
				match(KW_polar_a);
				break;
			case KW_polar_r:
				match(KW_polar_r);
				break;
			case KW_int:
				match(KW_int);
				break;
			case KW_float:
				match(KW_float);
				break;
			case KW_width:
				match(KW_width);
				break;
			case KW_height:
				match(KW_height);
				break;
			case KW_red:
				match(KW_red);
				break;
			case KW_green:
				match(KW_green);
				break;
			case KW_blue:
				match(KW_blue);
				break;
			default:
				match(KW_alpha);
				break;
		}
		
		return name;
	}
	
	public ExpressionPredefinedName predefinedName() throws SyntaxException {
		Token first = t;
		Token name = t;
		if (isKind(KW_Z)) {
			match(KW_Z);
		}
		else if (isKind(KW_default_height)) {
			match(Kind.KW_default_height);
		}
		else {
			match(Kind.KW_default_width);
		}
		
		return new ExpressionPredefinedName(first, name);
	}
	
	public StatementInput statementInput() throws SyntaxException {
		Token firstToken = t;
		match(KW_input);
		Token name = t;
		match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		Expression e = expression();
		return new StatementInput(firstToken, name, e);
	}
	
	public StatementWrite statementWrite() throws SyntaxException {
		Token firstToken = t;
		match(KW_write);
		Token sourceName = t;
		match(IDENTIFIER);
		match(KW_to);
		Token destName = t;
		match(IDENTIFIER);
		return new StatementWrite(firstToken, sourceName, destName);
	}
	
	public StatementAssign statementAssignment() throws SyntaxException {
		Token firstToken = t;
		LHS lhs = lhs();
		match(OP_ASSIGN);
		Expression e = expression();
		return new StatementAssign(firstToken, lhs, e);
	}
	
	public LHS lhs() throws SyntaxException {
		Token firstToken = t;
		LHS l = null;
		if (isKind(IDENTIFIER)) {
			Token name = t;
			l = new LHSIdent(firstToken, name);
			match(IDENTIFIER);
			if (isKind(LSQUARE)) {
				PixelSelector pixelSelector = pixelSelector();
				l = new LHSPixel(firstToken, name, pixelSelector);
			}
		} else {
			Token c = color();
			match(LPAREN);
			Token name = t;
			match(IDENTIFIER);
			PixelSelector pixelSelector = pixelSelector();
			match(RPAREN);
			l = new LHSSample(firstToken, name, pixelSelector, c);
		}
		
		return l;
	}
	
	public StatementWhile statementWhile() throws SyntaxException {
		Token firstToken = t;
		match(KW_while);
		match(LPAREN);
		Expression guard = expression();
		match(RPAREN);
		Block b = block();
		return new StatementWhile(firstToken, guard, b);
	}
	
	public StatementIf statementIf() throws SyntaxException {
		Token firstToken = t;
		match(KW_if);
		match(LPAREN);
		Expression guard = expression();
		match(RPAREN);
		Block b = block();
		return new StatementIf(firstToken, guard, b);
	}
	
	public StatementShow statementShow() throws SyntaxException {
		Token firstToken = t;
		match(KW_show);
		Expression e = expression();
		return new StatementShow(firstToken, e);
	}
	
	public StatementSleep statementSleep() throws SyntaxException {
		Token firstToken = t;
		match(KW_sleep);
		Expression duration = expression();
		return new StatementSleep(firstToken, duration);
	}
	
	public PixelSelector pixelSelector() throws SyntaxException {
		Token firstToken = t;
		match(LSQUARE);
		Expression ex = expression();
		match(COMMA);
		Expression ey = expression();
		match(RSQUARE);
		return new PixelSelector(firstToken, ex, ey);
	}
	
	public Token color() throws SyntaxException {
		Token c = null;
		if (isKind(KW_red)) {
			c = t;
			match(KW_red);
		} else if (isKind(KW_green)) {
			c = t;
			match(KW_green);
		} else if (isKind(KW_blue)) {
			c = t;
			match(KW_blue);
		} else {
			c = t;
			match(KW_alpha);
		}
		
		return c;
	}
	
	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}

	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		
		throw new SyntaxException(t, "Syntax Error at " + t.line() + ":" + t.posInLine() + ", expected: " + kind + ", found: " + t.kind);
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			throw new SyntaxException(t, "Syntax Error at " + t.line() + ":" + t.posInLine() + ", expected: " + EOF + ", found: " + t.kind);
			// Note that EOF should be matched by the matchEOF method which is called only
			// in parse().
			// Anywhere else is an error. */
		}
		
		t = scanner.nextToken();
		return tmp;
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t, "Syntax Error at " + t.line() + ":" + t.posInLine() + ", expected: " + t.kind + ", found: " + EOF);
	}

}
