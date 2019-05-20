package cop5556sp18;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.TypeChecker.SemanticException;

public class TypeCheckerTest {

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}



	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { show true+4; }"; //error, incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void declaration_int() throws Exception {
		String input = "prog{ int x; }";
		typeCheck(input);
	}
	
	@Test
	public void declaration_image() throws Exception {
		String input = "prog{ image x; }";
		typeCheck(input);
	}
	
	@Test
	public void declaration_error1() throws Exception {
		String input = "prog{ image x; image x; }";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void declaration_error2() throws Exception {
		String input = "prog{ image x; while (x < 30) { image x; image z; }; image z; }";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void declaration_image_size() throws Exception {
		String input = "prog{ image x[78, 90]; }";
		typeCheck(input);
	}
	
	@Test
	public void statement_input() throws Exception {
		String input = "prog { int z; input z from @ 4 + 10; }";
		typeCheck(input);
	}
	
	@Test
	public void statement_write() throws Exception {
		String input = "prog { image x; filename y; write x to y; }";
		typeCheck(input);
	}
	
	@Test
	public void statement_assign1() throws Exception {
		String input = "prog { int x; int y; y := x; }";
		typeCheck(input);
	}
	
	@Test
	public void statement_assign2() throws Exception {
		String input = "prog { filename x; filename y; y := x; }";
		typeCheck(input);
	}
	
	@Test
	public void expression_unary1() throws Exception {
		String input = "prog { int x; x := +2; }";
		typeCheck(input);
	}
	
	@Test
	public void expression_unary_error1() throws Exception {
		String input = "prog { int x; x := +true; }";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void show_undefined() throws Exception {
		String input = "prog { show x; }"; //error, incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	
	@Test
	public void statement_while1() throws Exception {
		String input = "prog { int x; int y; int z; while (x < y) { boolean y; y := x == 1; }; y := z;}";
		typeCheck(input);
	}
	
	@Test
	public void statement_while2() throws Exception {
		String input = "prog { int x; int y; int z; while (x <= y) { boolean y; y := x == 1; }; y := z;}";
		typeCheck(input);
	}
	
	@Test
	public void statement_while3() throws Exception {
		String input = "prog { int x; int y; int z; while (x == y) { boolean y; y := x == 1; }; y := z;}";
		typeCheck(input);
	}
	
	@Test
	public void statement_while_error1() throws Exception {
		String input = "prog { int x; int y; int z; while (x) { boolean y; y := x == 1; }; y := z;}";
		thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
	}
	
	@Test
	public void statement_while_scope_error1() throws Exception {
		String input = "prog { int y; int x; while (x < y) { int z; }; y := z; }";
		thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
	}

	@Test
	public void statement_while_scope1() throws Exception {
		String input = "prog { int y; int x; while (x < y) { int z; }; int z; x := y * x / z; }";
		typeCheck(input);
	}

	@Test
	public void statement_while_scope2() throws Exception {
		String input = "prog { int y; int x; while (x < y) { int z; }; boolean z; z := y>= x; if (z) { image z; }; }";
		typeCheck(input);
	}
	
	@Test
	public void testDemo1() throws Exception {
		String input = "demo1{image h;input h from @0;show h; sleep(4000);\n "
				+ "image g[width(h),height(h)];int x;x:=0;\n"
                + "while(x<width(g)){\n" 
				+ "int y;y:=0;\n"
                + "while(y<height(g))\n"
                + "{g[x,y]:=h[y,x];\n"
                + "y:=y+1;};\n"
                + "x:=x+1;};\n"
                + "show g;sleep(4000);\n"
                + "}";
		typeCheck(input);
    }
	
	
	@Test
	public void testProg1() throws Exception {
        String input = "makeRedImage{image im[256,256];"
        		+ "int x;int y;x:=0;y:=0;"
        		+ "while(x<width(im)) {"
        		+ "y:=0;while(y<height(im)) {"
        		+ "im[x,y]:=<<255,255,0,0>>"
        		+ ";y:=y+1;};x:=x+1;};show im;"
        		+ "}";
        typeCheck(input);
    }


	@Test
    public void testProg2() throws Exception {
        String input = "test{\n" +
        		"image obj[1024,1024];int x;x:=0;\n" +
        		"while(x<width(obj)) {\n" +
        		"int y;y:=0;\n" +
        		"while(y<height(obj)) {\n" +
        		"float p;p:=polar_r[x,y];\n" + 
        		"int r;r:=int(p)%Z;obj[x,y]:=<<Z,0,0,r>>;y:=y+1;\n" +
        		"};\n" + 
        		"x:=x+1;\n" + 
        		"};\n" + 
        		"show obj;\n" + 
        		"}";
        typeCheck(input);
    }


	@Test
    public void testProg3() throws Exception {
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
        typeCheck(input);
    }
	
	@Test
    public void testProg3_error3() throws Exception {
        String input = "test{image sky; input sky from @0;show"
        		+ " sky;sleep(4000);image sky2\n"
        		+ "[width(sky),true]\n"
        		+ ";int x;x\n"
        		+ ":=\n"
        		+ "0;while(x<width(sky2)) {\n"
        		+ "int y;y:=0;while(y<height(sky2)) {\n"
        		+ "blue(sky2[x,y]):=red(sky[x,y]);green(sky2[x,y]):=blue(sky[x,y]);\n"
        		+ "red(sky2[x,y]):=green(sky[x,y]);alpha(sky2[x,y]):=Z;y:=y+1;};x:=x+1;}\n"
        		+ ";show sky2;sleep(4000);}\n";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }
	
	@Test
    public void testProg3_error1() throws Exception {
        String input = "test{image sky; input sky from @0;show"
        		+ " sky;sleep(4000);image sky2\n"
        		+ "[width(sky),height(sky)]\n"
        		+ ";int x;x\n"
        		+ ":=\n"
        		+ "0;while(x<width(sky2)) {\n"
        		+ "int y;y:=0;while(y<height(sky2)) {\n"
        		+ "blue(sky2[x,y]):=red(sky[x,y]);green(sky2[x,y]):=blue(sky[x,y]);\n"
        		+ "red(sky2[x,y]):=green(sky[x,y]);alpha(sky2[x,y]):=Z;y:=y+1;};x:=x+1;}\n"
        		+ ";show sky2;sleep(4000.0);}\n";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }
	
	@Test
    public void testProg3_error2() throws Exception {
        String input = "test{image sky; input sky from @0;show"
        		+ " sky;sleep(4000);image sky2\n"
        		+ "[width(sky),height(sky)]\n"
        		+ ";float x;x\n"
        		+ ":=\n"
        		+ "0;while(x<width(sky2)) {\n"
        		+ "int y;y:=0;while(y<height(sky2)) {\n"
        		+ "blue(sky2[x,y]):=red(sky[x,y]);green(sky2[x,y]):=blue(sky[x,y]);\n"
        		+ "red(sky2[x,y]):=green(sky[x,y]);alpha(sky2[x,y]):=Z;y:=y+1;};x:=x+1;}\n"
        		+ ";show sky2;sleep(4000);}\n";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }
}