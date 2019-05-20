 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
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

import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;
import static cop5556sp18.Scanner.Kind.*;

public class ParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}	

	
	
	/**
	 * Checks that an element in a block is a declaration with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param name
	 * @return
	 */
	Declaration checkDec(Block block, int index, Kind type,
			String name) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(Declaration.class, node.getClass());
		Declaration dec = (Declaration) node;
		assertEquals(type, dec.type);
		assertEquals(name, dec.name);
		return dec;
	}	

	
	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "");
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "Program [progName=b, block=Block [decsOrStatements=[]]]");
	}	
	
	

	@Test
	public void testDemo1() throws LexicalException, SyntaxException {
		String input = "demo1{image h;input h from @0;show h; sleep(4000); "
				+ "image g[width(h),height(h)];int x;x:=0;"
                + "while(x<width(g)){int y;y:=0;while(y<height(g)"
                + "){g[x,y]:=h[y,x"
                + "];y:=y+1;};x:=x+1;};show g;sleep(4000)"
                + ";}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "Program [progName=demo1, block=Block [decsOrStatements=[Declaration [type=KW_image, name=h, width=null, height=null], StatementInput [destName=h, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=h]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=g, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=h]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=h]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=g, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixel [name=h, pixelSelector=PixelSelector [ex=ExpressionIdent [name=y], ey=ExpressionIdent [name=x]]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=g]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
    }
	
	
	@Test
	public void testProg1() throws LexicalException, SyntaxException {
        String input = "makeRedImage{image im[256,256];"
        		+ "int x;int y;x:=0;y:=0;"
        		+ "while(x<width(im)) {"
        		+ "y:=0;while(y<height(im)) {"
        		+ "im[x,y]:=<<255,255,0,0>>"
        		+ ";y:=y+1;};x:=x+1;};show im;"
        		+ "}";
        Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "Program [progName=makeRedImage, block=Block [decsOrStatements=[Declaration [type=KW_image, name=im, width=ExpressionIntegerLiteral [value=256], height=ExpressionIntegerLiteral [value=256]], Declaration [type=KW_int, name=x, width=null, height=null], Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=im, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixelConstructor [alpha=ExpressionIntegerLiteral [value=255], red=ExpressionIntegerLiteral [value=255], green=ExpressionIntegerLiteral [value=0], blue=ExpressionIntegerLiteral [value=0]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=im]]]]]");
    }


	@Test
    public void testProg2() throws LexicalException, SyntaxException {
        String input = "test{image obj[1024,1024];int x;x:=0;while(x<width(obj)) {int y;y:=0;while(y<height(obj)) {float p;p:=polar_r[x,y];int r;r:=int(p)%Z;obj[x,y]:=<<Z,0,0,r>>;y:=y+1;};x:=x+1;};show obj;}";
        Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "Program [progName=test, block=Block [decsOrStatements=[Declaration [type=KW_image, name=obj, width=ExpressionIntegerLiteral [value=1024], height=ExpressionIntegerLiteral [value=1024]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=obj]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=obj]]], b=Block [decsOrStatements=[Declaration [type=KW_float, name=p, width=null, height=null], StatementAssign [lhs=LHSIdent [name=p], e=ExpressionFunctionAppWithPixel [name=KW_polar_r, e0=ExpressionIdent [name=x], e1=ExpressionIdent [name=y]]], Declaration [type=KW_int, name=r, width=null, height=null], StatementAssign [lhs=LHSIdent [name=r], e=ExpressionBinary [leftExpression=ExpressionFunctionApp [function=KW_int, e=ExpressionIdent [name=p]], op=OP_MOD, rightExpression=ExpressionPredefinedName [name=KW_Z]]], StatementAssign [lhs=LHSPixel [name=obj, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixelConstructor [alpha=ExpressionPredefinedName [name=KW_Z], red=ExpressionIntegerLiteral [value=0], green=ExpressionIntegerLiteral [value=0], blue=ExpressionIdent [name=r]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=obj]]]]]");
    }


	@Test
    public void testProg3() throws LexicalException, SyntaxException {
        String input = "test{image sky; input sky from @0;show"
        		+ " sky;sleep(4000);image sky2\n"
        		+ "[width(sky),height(sky)]\n"
        		+ ";int x;x\n"
        		+ ":=\n"
        		+ "0;while(x<width(sky2)) {\n"
        		+ "int y;y:=0;while(y<height(sky2)) {\n"
        		+ "blue(sky2[x,y]):=red(sky[x,y]);green(sky2[x,y]):=blue(sky[x,y]);\n"
        		+ "red(sky2[x,y]):=green(sky[x,y]);alpha(sky2[x,y]):=Z;y:=y+1;};x:=x+1;}\n"
        		+ ";show sky2;sleep(4000);}\n";
        Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "Program [progName=test, block=Block [decsOrStatements=[Declaration [type=KW_image, name=sky, width=null, height=null], StatementInput [destName=sky, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=sky]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=sky2, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=sky]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=sky]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=sky2]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=sky2]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSSample [name=sky2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_blue], e=ExpressionFunctionApp [function=KW_red, e=ExpressionPixel [name=sky, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=sky2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_green], e=ExpressionFunctionApp [function=KW_blue, e=ExpressionPixel [name=sky, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=sky2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_red], e=ExpressionFunctionApp [function=KW_green, e=ExpressionPixel [name=sky, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=sky2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_alpha], e=ExpressionPredefinedName [name=KW_Z]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=sky2]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
    }
	
	@Test
	public void testBlock() throws LexicalException, SyntaxException {
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("test {");
		lines.add("int x;");
		lines.add("float x;");
		lines.add("boolean yy;");
		lines.add("image im;");
		lines.add("filename fn;");
		lines.add("image img [4, 5];");
		lines.add("write something to somewhere;");
		lines.add("}");
		String input = String.join("\n", lines);
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "Program [progName=test, block=Block [decsOrStatements=[Declaration [type=KW_int, name=x, width=null, height=null], Declaration [type=KW_float, name=x, width=null, height=null], Declaration [type=KW_boolean, name=yy, width=null, height=null], Declaration [type=KW_image, name=im, width=null, height=null], Declaration [type=KW_filename, name=fn, width=null, height=null], Declaration [type=KW_image, name=img, width=ExpressionIntegerLiteral [value=4], height=ExpressionIntegerLiteral [value=5]], StatementWrite [sourceName=something, destName=somewhere]]]]");
	}
	
	
	@Test
	public void testStatement() throws LexicalException, SyntaxException {
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("test {");
		lines.add("x := +-55;");
		lines.add("x := inden**8**+-!true;");
		lines.add("if (++!<<a,b,c,d>>**some)");
		lines.add("{");
		lines.add("show x ? y : 2+ 4 * 5;");
		lines.add("red(abc[5+7*2+seven, 90]) := seven & a & b | d == c != d != c < q > p > 5 >= 4 <= sk + a -b +2 *qq*yu/dn%q**(c**6);");
		lines.add("ident := abs[5+default_height*2+seven, 7 * Z];");
		lines.add("};");
		lines.add("while (");
		lines.add("inden**8**+-!true");
		lines.add(") {");
		lines.add("red ( c [7, y]) := val;");
		lines.add("};");
		lines.add("}");
		String input = String.join("\n", lines);
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "Program [progName=test, block=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=x], e=ExpressionUnary [op=OP_PLUS, expression=ExpressionUnary [op=OP_MINUS, expression=ExpressionIntegerLiteral [value=55]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=inden], op=OP_POWER, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=8], op=OP_POWER, rightExpression=ExpressionUnary [op=OP_PLUS, expression=ExpressionUnary [op=OP_MINUS, expression=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionBooleanLiteral [value=true]]]]]]], StatementIf [guard=ExpressionBinary [leftExpression=ExpressionUnary [op=OP_PLUS, expression=ExpressionUnary [op=OP_PLUS, expression=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionPixelConstructor [alpha=ExpressionIdent [name=a], red=ExpressionIdent [name=b], green=ExpressionIdent [name=c], blue=ExpressionIdent [name=d]]]]], op=OP_POWER, rightExpression=ExpressionIdent [name=some]], b=Block [decsOrStatements=[ShowStatement [e=ExpressionConditional [guard=ExpressionIdent [name=x], trueExpression=ExpressionIdent [name=y], falseExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=2], op=OP_PLUS, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=4], op=OP_TIMES, rightExpression=ExpressionIntegerLiteral [value=5]]]]], StatementAssign [lhs=LHSSample [name=abc, pixelSelector=PixelSelector [ex=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=5], op=OP_PLUS, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=7], op=OP_TIMES, rightExpression=ExpressionIntegerLiteral [value=2]]], op=OP_PLUS, rightExpression=ExpressionIdent [name=seven]], ey=ExpressionIntegerLiteral [value=90]], color=KW_red], e=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIdent [name=seven], op=OP_AND, rightExpression=ExpressionIdent [name=a]], op=OP_AND, rightExpression=ExpressionIdent [name=b]], op=OP_OR, rightExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIdent [name=d], op=OP_EQ, rightExpression=ExpressionIdent [name=c]], op=OP_NEQ, rightExpression=ExpressionIdent [name=d]], op=OP_NEQ, rightExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIdent [name=c], op=OP_LT, rightExpression=ExpressionIdent [name=q]], op=OP_GT, rightExpression=ExpressionIdent [name=p]], op=OP_GT, rightExpression=ExpressionIntegerLiteral [value=5]], op=OP_GE, rightExpression=ExpressionIntegerLiteral [value=4]], op=OP_LE, rightExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIdent [name=sk], op=OP_PLUS, rightExpression=ExpressionIdent [name=a]], op=OP_MINUS, rightExpression=ExpressionIdent [name=b]], op=OP_PLUS, rightExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=2], op=OP_TIMES, rightExpression=ExpressionIdent [name=qq]], op=OP_TIMES, rightExpression=ExpressionIdent [name=yu]], op=OP_DIV, rightExpression=ExpressionIdent [name=dn]], op=OP_MOD, rightExpression=ExpressionBinary [leftExpression=ExpressionIdent [name=q], op=OP_POWER, rightExpression=ExpressionBinary [leftExpression=ExpressionIdent [name=c], op=OP_POWER, rightExpression=ExpressionIntegerLiteral [value=6]]]]]]]]], StatementAssign [lhs=LHSIdent [name=ident], e=ExpressionFunctionAppWithPixel [name=KW_abs, e0=ExpressionBinary [leftExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=5], op=OP_PLUS, rightExpression=ExpressionBinary [leftExpression=ExpressionPredefinedName [name=KW_default_height], op=OP_TIMES, rightExpression=ExpressionIntegerLiteral [value=2]]], op=OP_PLUS, rightExpression=ExpressionIdent [name=seven]], e1=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=7], op=OP_TIMES, rightExpression=ExpressionPredefinedName [name=KW_Z]]]]]]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=inden], op=OP_POWER, rightExpression=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=8], op=OP_POWER, rightExpression=ExpressionUnary [op=OP_PLUS, expression=ExpressionUnary [op=OP_MINUS, expression=ExpressionUnary [op=OP_EXCLAMATION, expression=ExpressionBooleanLiteral [value=true]]]]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSSample [name=c, pixelSelector=PixelSelector [ex=ExpressionIntegerLiteral [value=7], ey=ExpressionIdent [name=y]], color=KW_red], e=ExpressionIdent [name=val]]]]]]]]");
	}
	
	
	@Test
	public void testSemiException() throws LexicalException, SyntaxException {
		String input = "test {hello :=9}";  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "");
	}
	
	
	@Test
	public void testDummy() throws LexicalException, SyntaxException {
		String input = "prog {input abe from @ ((123 <= 123 ))?((123)):((123));}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(), "Program [progName=prog, block=Block [decsOrStatements=[StatementInput [destName=abe, e=ExpressionConditional [guard=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=123], op=OP_LE, rightExpression=ExpressionIntegerLiteral [value=123]], trueExpression=ExpressionIntegerLiteral [value=123], falseExpression=ExpressionIntegerLiteral [value=123]]]]]]");
	}
	
	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		String input = "x + 2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}
	
	
	
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c; image j;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		checkDec(p.block, 0, Kind.KW_int, "c");
		checkDec(p.block, 1, Kind.KW_image, "j");
	}
	
	
	@Test
	public void testStatement1() throws LexicalException, SyntaxException {
		String input = "show hello + 78";
		Parser parser = makeParser(input);
		Statement p = parser.statement();
		show(p);
		assertEquals(p.toString(), "ShowStatement [e=ExpressionBinary [leftExpression=ExpressionIdent [name=hello], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=78]]]");
	}
	
	
	@Test
	public void testUnary() throws LexicalException, SyntaxException {
		String input = "+2";
		Parser parser = makeParser(input);
		Expression p = parser.expression();
		show(p);
		//assertEquals(p.toString(), "ShowStatement [e=ExpressionBinary [leftExpression=ExpressionIdent [name=hello], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=78]]]");
	}
	
	
	@Test
	public void testDeclaration1() throws LexicalException, SyntaxException {
		String input = "image a[70, 8 + b]";
		Parser parser = makeParser(input);
		Declaration p = parser.declaration();
		show(p);
		assertEquals(p.toString(), "Declaration [type=KW_image, name=a, width=ExpressionIntegerLiteral [value=70], height=ExpressionBinary [leftExpression=ExpressionIntegerLiteral [value=8], op=OP_PLUS, rightExpression=ExpressionIdent [name=b]]]");
	}
}
	

