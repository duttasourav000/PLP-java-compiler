package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
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
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

public class TypeChecker implements ASTVisitor {

	SymbolTable symbolTable;
	
	TypeChecker() {
		this.symbolTable = new SymbolTable();
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	public static Type getExpressionInferredType(Type type1, Type type2, Kind op) {
		if (type1 == Type.INTEGER && type2 == Type.INTEGER &&
			(op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || 
			 op == Kind.OP_MOD || op == Kind.OP_POWER || op == Kind.OP_AND || op == Kind.OP_OR)) {
			return Type.INTEGER;
		}
		
		if (type1 == Type.FLOAT && type2 == Type.FLOAT &&
			(op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || 
			 op == Kind.OP_POWER)) {
			return Type.FLOAT;
		}
		
		if (type1 == Type.FLOAT && type2 == Type.INTEGER &&
			(op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || 
			 op == Kind.OP_POWER)) {
			return Type.FLOAT;
		}
		
		if (type1 == Type.INTEGER && type2 == Type.FLOAT &&
			(op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || 
			 op == Kind.OP_POWER)) {
			return Type.FLOAT;
		}
		
		if (type1 == Type.BOOLEAN && type2 == Type.BOOLEAN &&
			(op == Kind.OP_AND || op == Kind.OP_OR)) {
			return Type.BOOLEAN;
		}

		if (type1 == Type.INTEGER && type2 == Type.INTEGER &&
			(op == Kind.OP_AND || op == Kind.OP_OR)) {
			return Type.INTEGER;
		}
		
		if (type1 == Type.INTEGER && type2 == Type.INTEGER &&
			(op == Kind.OP_EQ || op == Kind.OP_NEQ || op == Kind.OP_GT || op == Kind.OP_GE || 
			 op == Kind.OP_LT || op == Kind.OP_LE)) {
			return Type.BOOLEAN;
		}
		
		if (type1 == Type.FLOAT && type2 == Type.FLOAT &&
			(op == Kind.OP_EQ || op == Kind.OP_NEQ || op == Kind.OP_GT || op == Kind.OP_GE || 
			 op == Kind.OP_LT || op == Kind.OP_LE)) {
			return Type.BOOLEAN;
		}
		
		if (type1 == Type.BOOLEAN && type2 == Type.BOOLEAN &&
			(op == Kind.OP_EQ || op == Kind.OP_NEQ || op == Kind.OP_GT || op == Kind.OP_GE || 
			 op == Kind.OP_LT || op == Kind.OP_LE)) {
			return Type.BOOLEAN;
		}
		
		return null;
	}
	
	public Type getExpressionArgInferredType(Kind functionName, Type type) {
		if (type == Type.INTEGER &&
				(functionName == Kind.KW_abs ||
				 functionName == Kind.KW_red ||
				 functionName == Kind.KW_green ||
				 functionName == Kind.KW_blue ||
				 functionName == Kind.KW_alpha)) {
			return Type.INTEGER;
		}
		
		if (type == Type.FLOAT &&
				(functionName == Kind.KW_abs ||
				 functionName == Kind.KW_sin ||
				 functionName == Kind.KW_cos ||
				 functionName == Kind.KW_atan ||
				 functionName == Kind.KW_log)) {
			return Type.FLOAT;
		}
		
		if (type == Type.IMAGE &&
				(functionName == Kind.KW_width ||
				 functionName == Kind.KW_height)) {
			return Type.INTEGER;
		}
		
		if (type == Type.INTEGER &&
				functionName == Kind.KW_float) {
			return Type.FLOAT;
		}
		
		if (type == Type.FLOAT &&
				functionName == Kind.KW_float) {
			return Type.FLOAT;
		}
		
		if (type == Type.FLOAT &&
				functionName == Kind.KW_int) {
			return Type.INTEGER;
		}
		
		if (type == Type.INTEGER &&
				functionName == Kind.KW_int) {
			return Type.INTEGER;
		}
		
		return null;
	}
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		this.symbolTable.enterScope();
		for (int i = 0; i < block.decsOrStatements.size(); i++)
		{
			block.decsOrStatements.get(i).visit(this, arg);
		}
		
		this.symbolTable.closeScope();
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration entry = this.symbolTable.lookup(declaration.name);
		Integer scopeNumber = this.symbolTable.lookupScope(declaration.name);
		if (entry != null && scopeNumber != null && scopeNumber == this.symbolTable.currentScope) {
			throw new SemanticException(declaration.firstToken, String.format("Error: line %s, pos %s, %s + already defined.", declaration.firstToken.line(), declaration.firstToken.posInLine(), declaration.name));
		}
		
		if (declaration.width == null && declaration.height == null) {
			// do nothing
		}
		else {
			if (declaration.width == null) {
				throw new SemanticException(declaration.firstToken, String.format("Error: line %s, pos %s, Expected not null integer.", declaration.firstToken.line(), declaration.firstToken.posInLine()));
			}
			
			declaration.width.visit(this, arg);
			if (declaration.width.type != Type.INTEGER) {
				throw new SemanticException(declaration.firstToken, String.format("Error: line %s, pos %s, Expected integer.", declaration.firstToken.line(), declaration.firstToken.posInLine()));
			}
			
			if (declaration.height == null) {
				throw new SemanticException(declaration.height.firstToken, String.format("Error: line %s, pos %s, Expected not null integer.", declaration.firstToken.line(), declaration.firstToken.posInLine()));
			}
			
			declaration.height.visit(this, arg);
			if (declaration.height.type != Type.INTEGER) {
				throw new SemanticException(declaration.height.firstToken, String.format("Error: line %s, pos %s, Expected integer.", declaration.firstToken.line(), declaration.firstToken.posInLine()));
			}
		}
		
		this.symbolTable.add(declaration);
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementWrite.sourceDec = this.symbolTable.lookup(statementWrite.sourceName);
		if (statementWrite.sourceDec == null) {
			throw new SemanticException(statementWrite.firstToken, String.format("Error: line %s, pos %s, Expected indentifier.", statementWrite.firstToken.line(), statementWrite.firstToken.posInLine()));
		}
		
		statementWrite.destDec = this.symbolTable.lookup(statementWrite.destName);
		if (statementWrite.destDec == null) {
			throw new SemanticException(statementWrite.firstToken, String.format("Error: line %s, pos %s, Expected indentifier.", statementWrite.firstToken.line(), statementWrite.firstToken.posInLine()));
		}
		
		if (Types.getType(statementWrite.sourceDec.type) != Type.IMAGE) {
			throw new SemanticException(statementWrite.firstToken, String.format("Error: line %s, pos %s, Expected image.", statementWrite.firstToken.line(), statementWrite.firstToken.posInLine()));
		}
		
		if (Types.getType(statementWrite.destDec.type) != Type.FILE) {
			throw new SemanticException(statementWrite.firstToken, String.format("Error: line %s, pos %s, Expected filename.", statementWrite.firstToken.line(), statementWrite.firstToken.posInLine()));
		}
		
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementInput.dec = this.symbolTable.lookup(statementInput.destName);
		if (statementInput.dec == null) {
			throw new SemanticException(statementInput.firstToken, String.format("Error: line %s, pos %s, Undefined identifier %s.", statementInput.firstToken.line(), statementInput.firstToken.posInLine(), statementInput.destName));
		}
		
		statementInput.e.visit(this, arg);
		if (statementInput.e.type != Type.INTEGER) {
			throw new SemanticException(statementInput.e.firstToken, String.format("Error: line %s, pos %s, Expected integer.", statementInput.firstToken.line(), statementInput.firstToken.posInLine()));
		}
		
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
		
		if (pixelSelector.ex.type != pixelSelector.ey.type) {
			throw new SemanticException(pixelSelector.ey.firstToken, String.format("Error: line %s, pos %s, Unequal types.", pixelSelector.ey.firstToken.line(), pixelSelector.ey.firstToken.posInLine()));
		}

		if (pixelSelector.ex.type != Type.INTEGER &&
				pixelSelector.ex.type != Type.FLOAT) {
			throw new SemanticException(pixelSelector.ex.firstToken, String.format("Error: line %s, pos %s, Expected integer or float expression.", pixelSelector.ex.firstToken.line(), pixelSelector.ex.firstToken.posInLine()));
		}
				
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionConditional.guard.visit(this, arg);
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		
		if (expressionConditional.guard.type != Type.BOOLEAN) {
			throw new SemanticException(expressionConditional.guard.firstToken, String.format("Error: line %s, pos %s, Expected boolean expression.", expressionConditional.guard.firstToken.line(), expressionConditional.guard.firstToken.posInLine()));
		}
		
		if (expressionConditional.trueExpression.type != expressionConditional.falseExpression.type) {
			throw new SemanticException(expressionConditional.trueExpression.firstToken, String.format("Error: line %s, pos %s, Unequal types.", expressionConditional.trueExpression.firstToken.line(), expressionConditional.trueExpression.firstToken.posInLine()));
		}
		
		expressionConditional.type = expressionConditional.trueExpression.type;
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		expressionBinary.type = getExpressionInferredType(expressionBinary.leftExpression.type, expressionBinary.rightExpression.type, expressionBinary.op);

		if (expressionBinary.type == null) {
			throw new SemanticException(expressionBinary.firstToken, String.format("Error: line %s, pos %s, Unsupported operation.", expressionBinary.firstToken.line(), expressionBinary.firstToken.posInLine()));
		}
		
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		expressionUnary.type = expressionUnary.expression.type;
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionIntegerLiteral.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBooleanLiteral.type = Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPredefinedName.type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFloatLiteral.type = Type.FLOAT;
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		
		// TODO Auto-generated method stub
		expressionFunctionAppWithExpressionArg.type = getExpressionArgInferredType(expressionFunctionAppWithExpressionArg.function, expressionFunctionAppWithExpressionArg.e.type);	
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionFunctionAppWithPixel.e0.visit(this, arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		
		if (expressionFunctionAppWithPixel.name == Kind.KW_cart_x ||
				expressionFunctionAppWithPixel.name == Kind.KW_cart_y) {
			if (expressionFunctionAppWithPixel.e0.type != Type.FLOAT ||
					expressionFunctionAppWithPixel.e1.type != Type.FLOAT) {
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken, String.format("Error: line %s, pos %s, Expected float.", expressionFunctionAppWithPixel.firstToken.line(), expressionFunctionAppWithPixel.firstToken.posInLine()));
			}
			
			expressionFunctionAppWithPixel.type = Type.INTEGER;
		}		
		else if (expressionFunctionAppWithPixel.name == Kind.KW_polar_a ||
				expressionFunctionAppWithPixel.name == Kind.KW_polar_r) {
			if (expressionFunctionAppWithPixel.e0.type != Type.INTEGER ||
					expressionFunctionAppWithPixel.e1.type != Type.INTEGER) {
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken, String.format("Error: line %s, pos %s, Expected integer.", expressionFunctionAppWithPixel.firstToken.line(), expressionFunctionAppWithPixel.firstToken.posInLine()));
			}
			
			expressionFunctionAppWithPixel.type = Type.FLOAT;
		}
		else {
			throw new SemanticException(expressionFunctionAppWithPixel.firstToken, String.format("Error: line %s, pos %s, Unexpected funtion.", expressionFunctionAppWithPixel.firstToken.line(), expressionFunctionAppWithPixel.firstToken.posInLine()));
		}
		
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		
		if (expressionPixelConstructor.alpha.type != Type.INTEGER) {
			throw new SemanticException(expressionPixelConstructor.alpha.firstToken, String.format("Error: line %s, pos %s, Expected integer.", expressionPixelConstructor.alpha.firstToken.line(), expressionPixelConstructor.alpha.firstToken.posInLine()));
		}
		
		if (expressionPixelConstructor.red.type != Type.INTEGER) {
			throw new SemanticException(expressionPixelConstructor.red.firstToken, String.format("Error: line %s, pos %s, Expected integer.", expressionPixelConstructor.red.firstToken.line(), expressionPixelConstructor.red.firstToken.posInLine()));
		}
		
		if (expressionPixelConstructor.green.type != Type.INTEGER) {
			throw new SemanticException(expressionPixelConstructor.green.firstToken, String.format("Error: line %s, pos %s, Expected integer.", expressionPixelConstructor.green.firstToken.line(), expressionPixelConstructor.green.firstToken.posInLine()));
		}
		
		if (expressionPixelConstructor.blue.type != Type.INTEGER) {
			throw new SemanticException(expressionPixelConstructor.blue.firstToken, String.format("Error: line %s, pos %s, Expected integer.", expressionPixelConstructor.blue.firstToken.line(), expressionPixelConstructor.blue.firstToken.posInLine()));
		}
		
		expressionPixelConstructor.type = Type.INTEGER;
		return expressionPixelConstructor;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementAssign.lhs.visit(this, arg);
		statementAssign.e.visit(this, arg);
		
		if (statementAssign.lhs.type != statementAssign.e.type) {
			throw new SemanticException(statementAssign.firstToken, String.format("Error: line %s, pos %s, Unequal types.", statementAssign.firstToken.line(), statementAssign.firstToken.posInLine()));
		}
		
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		statementShow.e.visit(this, arg);
		
		// TODO Auto-generated method stub
		if (statementShow.e.type != Type.INTEGER &&
				statementShow.e.type != Type.BOOLEAN &&
				statementShow.e.type != Type.FLOAT &&
				statementShow.e.type != Type.IMAGE) {
			throw new SemanticException(statementShow.e.firstToken, String.format("Error: line %s, pos %s, Expected integer, boolean, float or image.", statementShow.e.firstToken.line(), statementShow.e.firstToken.posInLine()));
		}
		
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionPixel.dec = this.symbolTable.lookup(expressionPixel.name);
		if (expressionPixel.dec == null) {
			throw new SemanticException(expressionPixel.firstToken, String.format("Error: line %s, pos %s, Undefined identifier %s", expressionPixel.firstToken.line(), expressionPixel.firstToken.posInLine(), expressionPixel.name));
		}
		
		expressionPixel.pixelSelector.visit(this, arg);
		if (Types.getType(expressionPixel.dec.type) != Type.IMAGE) {
			throw new SemanticException(expressionPixel.firstToken, String.format("Error: line %s, pos %s, Expected image %s.", expressionPixel.firstToken.line(), expressionPixel.firstToken.posInLine(), expressionPixel.dec.name));
		}
		
		expressionPixel.type = Type.INTEGER;
		
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionIdent.dec = this.symbolTable.lookup(expressionIdent.name);
		if (expressionIdent.dec == null) {
			throw new SemanticException(expressionIdent.firstToken, String.format("Error: line %s, pos %s, Undefined identifier %s.", expressionIdent.firstToken.line(), expressionIdent.firstToken.posInLine(), expressionIdent.name));
		}
		
		expressionIdent.type = Types.getType(expressionIdent.dec.type);		
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsSample.dec = this.symbolTable.lookup(lhsSample.name);
		if (lhsSample.dec == null) {
			throw new SemanticException(lhsSample.firstToken, String.format("Error: line %s, pos %s, Undefined identifier %s.", lhsSample.firstToken.line(), lhsSample.firstToken.posInLine(), lhsSample.name));
		}
		
		lhsSample.pixelSelector.visit(this, arg);
		if (Types.getType(lhsSample.dec.type) != Type.IMAGE) {
			throw new SemanticException(lhsSample.firstToken, String.format("Error: line %s, pos %s, Expected image %s.", lhsSample.dec.firstToken.line(), lhsSample.dec.firstToken.posInLine(), lhsSample.dec.name));
		}
		
		lhsSample.type = Type.INTEGER;
		
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsPixel.dec = this.symbolTable.lookup(lhsPixel.name);
		if (lhsPixel.dec == null) {
			throw new SemanticException(lhsPixel.firstToken, String.format("Error: line %s, pos %s, Undefined identifier %s.", lhsPixel.firstToken.line(), lhsPixel.firstToken.posInLine(), lhsPixel.name));
		}
		
		lhsPixel.pixelSelector.visit(this, arg);
		if (Types.getType(lhsPixel.dec.type) != Type.IMAGE) {
			throw new SemanticException(lhsPixel.firstToken, String.format("Error: line %s, pos %s, Expected image.", lhsPixel.firstToken.line(), lhsPixel.firstToken.posInLine()));
		}
		
		lhsPixel.type = Type.INTEGER;
		
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsIdent.dec = this.symbolTable.lookup(lhsIdent.name);
		if (lhsIdent.dec == null) {
			throw new SemanticException(lhsIdent.firstToken, String.format("Error: line %s, pos %s, Expected identifier.", lhsIdent.firstToken.line(), lhsIdent.firstToken.posInLine()));
		}
		
		lhsIdent.type = Types.getType(lhsIdent.dec.type);
		
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementIf.guard.visit(this, arg);
		if (statementIf.guard.type != Type.BOOLEAN) {
			throw new SemanticException(statementIf.guard.firstToken, String.format("Error: line %s, pos %s, Expected boolean.", statementIf.guard.firstToken.line(), statementIf.guard.firstToken.posInLine()));
		}
		
		statementIf.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementWhile.guard.visit(this, arg);
		if (statementWhile.guard.type != Type.BOOLEAN) {
			throw new SemanticException(statementWhile.guard.firstToken, String.format("Error: line %s, pos %s, Expected boolean.", statementWhile.guard.firstToken.line(), statementWhile.guard.firstToken.posInLine()));
		}
		
		statementWhile.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementSleep.duration.visit(this, arg);
		if (statementSleep.duration.type != Type.INTEGER) {
			throw new SemanticException(statementSleep.duration.firstToken, String.format("Error: line %s, pos %s, Expected integer.", statementSleep.duration.firstToken.line(), statementSleep.duration.firstToken.posInLine()));
		}
		
		return null;
	}


}
