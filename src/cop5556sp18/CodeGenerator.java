/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
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
import cop5556sp18.Scanner.Kind;
import cop5556sp18.CodeGenUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

public class CodeGenerator implements ASTVisitor, Opcodes {

	class VariableInfo {
		Label startLabel;
		String name;
		String type;
		int slotNumber;
		
		VariableInfo(Label startLabel, String name, String type, int slotNumber) {
			this.startLabel = startLabel;
			this.name = name;
			this.type = type;
			this.slotNumber = slotNumber;
		}
	}
	
	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	int slotNumber = 1;
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		ArrayList<VariableInfo> vis = new ArrayList<VariableInfo>();
		for (ASTNode node : block.decsOrStatements) {
			Object o = node.visit(this, null);
			if (o instanceof VariableInfo) {
				VariableInfo vi = (VariableInfo)o;
				if (vi != null) {
					vis.add(vi);
				}
			}
		}
		
		Label lb = new Label();
		mv.visitLabel(lb);
		for (VariableInfo vi : vis) {
			mv.visitLocalVariable(vi.name, vi.type, null, vi.startLabel, lb, vi.slotNumber);
		}
		
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		VariableInfo vi = null;
		switch (declaration.type) {
			case KW_int:
				Label ld1 = new Label();
				mv.visitLabel(ld1);
				vi = new VariableInfo(ld1, declaration.name, "I", this.slotNumber);
				//fv = cw.visitField(0, declaration.name, "I", null, null);
				//fv.visitEnd();
				break;
			case KW_float:
				Label ld2 = new Label();
				mv.visitLabel(ld2);
				vi = new VariableInfo(ld2, declaration.name, "F", this.slotNumber);
				//fv = cw.visitField(0, declaration.name, "F", null, null);
				//fv.visitEnd();
				break;
			case KW_boolean:
				Label ld3 = new Label();
				mv.visitLabel(ld3);
				vi = new VariableInfo(ld3, declaration.name, "Z", this.slotNumber);
				//fv = cw.visitField(0, declaration.name, "Z", null, null);
				//fv.visitEnd();
				break;
			case KW_image:
				Label ld4 = new Label();
				mv.visitLabel(ld4);
				vi = new VariableInfo(ld4, declaration.name, "Ljava/awt/image/BufferedImage;", this.slotNumber);
				//fv = cw.visitField(0, declaration.name, "Ljava/awt/image/BufferedImage;", null, null);
				if (declaration.width != null && declaration.height != null) {
					declaration.width.visit(this, arg);
					declaration.height.visit(this, arg);
				}
				else {
					mv.visitIntInsn(SIPUSH, this.defaultWidth);
					mv.visitIntInsn(SIPUSH, this.defaultHeight);
				}
				
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", RuntimeImageSupport.makeImageSig, false);
				mv.visitVarInsn(ASTORE, this.slotNumber);
				//fv.visitEnd();
				break;
			case KW_filename:
				Label ld5 = new Label();
				mv.visitLabel(ld5);
				vi = new VariableInfo(ld5, declaration.name, "Ljava/lang/String;", this.slotNumber);
				//fv = cw.visitField(0, declaration.name, "Ljava/lang/String;", null, null);
				//fv.visitEnd();
				break;
			default:
				throw new UnsupportedOperationException();
		}
		
		declaration.slotNumber = this.slotNumber;
		this.slotNumber++;
		return vi;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBinary.leftExpression.visit(this, arg);
		if (expressionBinary.op == Kind.OP_POWER) {
			if (expressionBinary.leftExpression.getType() == Type.INTEGER) {
				mv.visitInsn(I2D);
			}
			else if (expressionBinary.leftExpression.getType() == Type.FLOAT) {
				mv.visitInsn(F2D);
			}
		}
		else {
			if (expressionBinary.getType() == Type.FLOAT) {
				if (expressionBinary.leftExpression.getType() == Type.INTEGER) {
					mv.visitInsn(I2F);
				}
			}
		}
		
		expressionBinary.rightExpression.visit(this, arg);
		if (expressionBinary.op == Kind.OP_POWER) {
			if (expressionBinary.rightExpression.getType() == Type.INTEGER) {
				mv.visitInsn(I2D);
			}
			else if (expressionBinary.rightExpression.getType() == Type.FLOAT) {
				mv.visitInsn(F2D);
			}
		}
		else {
			if (expressionBinary.getType() == Type.FLOAT) {
				if (expressionBinary.rightExpression.getType() == Type.INTEGER) {
					mv.visitInsn(I2F);
				}
			}
		}
		
		if (expressionBinary.getType() == Type.BOOLEAN) {
			if (expressionBinary.op == Kind.OP_AND) {
				mv.visitInsn(IAND);
			}
			else if (expressionBinary.op == Kind.OP_OR) {
				mv.visitInsn(IOR);
			}
			else if (expressionBinary.op == Kind.OP_EQ) {
				if (expressionBinary.leftExpression.getType() == Type.FLOAT && expressionBinary.rightExpression.getType() == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					Label l3 = new Label();
					mv.visitJumpInsn(IFNE, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
				else {
					Label l3 = new Label();
					mv.visitJumpInsn(IF_ICMPNE, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
			}
			else if (expressionBinary.op == Kind.OP_NEQ) {
				if (expressionBinary.leftExpression.getType() == Type.FLOAT && expressionBinary.rightExpression.getType() == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					Label l3 = new Label();
					mv.visitJumpInsn(IFEQ, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
				else {
					Label l3 = new Label();
					mv.visitJumpInsn(IF_ICMPEQ, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
			}
			else if (expressionBinary.op == Kind.OP_GT) {
				if (expressionBinary.leftExpression.getType() == Type.FLOAT && expressionBinary.rightExpression.getType() == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					Label l3 = new Label();
					mv.visitJumpInsn(IFLE, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
				else {
					Label l3 = new Label();
					mv.visitJumpInsn(IF_ICMPLE, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
			}
			else if (expressionBinary.op == Kind.OP_GE) {
				if (expressionBinary.leftExpression.getType() == Type.FLOAT && expressionBinary.rightExpression.getType() == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					Label l3 = new Label();
					mv.visitJumpInsn(IFLT, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
				else {
					Label l3 = new Label();
					mv.visitJumpInsn(IF_ICMPLT, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
			}
			else if (expressionBinary.op == Kind.OP_LT) {
				if (expressionBinary.leftExpression.getType() == Type.FLOAT && expressionBinary.rightExpression.getType() == Type.FLOAT) {
					mv.visitInsn(FCMPG);
					Label l3 = new Label();
					mv.visitJumpInsn(IFGE, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
				else {
					Label l3 = new Label();
					mv.visitJumpInsn(IF_ICMPGE, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
			}
			else if (expressionBinary.op == Kind.OP_LE) {
				if (expressionBinary.leftExpression.getType() == Type.FLOAT && expressionBinary.rightExpression.getType() == Type.FLOAT) {
					mv.visitInsn(FCMPG);
					Label l3 = new Label();
					mv.visitJumpInsn(IFGT, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.FLOAT, Opcodes.FLOAT}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
				else {
					Label l3 = new Label();
					mv.visitJumpInsn(IF_ICMPGT, l3);
					mv.visitInsn(ICONST_1);
					Label l4 = new Label();
					mv.visitJumpInsn(GOTO, l4);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l4);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}
			}
			else {
				throw new UnsupportedOperationException();
			}
		}
		else if (expressionBinary.getType() == Type.INTEGER) {
			if (expressionBinary.op == Kind.OP_PLUS){
				mv.visitInsn(IADD);
			}
			else if (expressionBinary.op == Kind.OP_MINUS){
				mv.visitInsn(ISUB);
			}
			else if (expressionBinary.op == Kind.OP_TIMES){
				mv.visitInsn(IMUL);
			}
			else if (expressionBinary.op == Kind.OP_DIV){
				mv.visitInsn(IDIV);
			}
			else if (expressionBinary.op == Kind.OP_MOD){
				mv.visitInsn(IREM);
			}
			else if (expressionBinary.op == Kind.OP_POWER){
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2I);
			}
			else if (expressionBinary.op == Kind.OP_AND){
				mv.visitInsn(IAND);
			}
			else if (expressionBinary.op == Kind.OP_OR){
				mv.visitInsn(IOR);
			}
			else {
				throw new UnsupportedOperationException();
			}
		}
		else if (expressionBinary.getType() == Type.FLOAT) {
			if (expressionBinary.op == Kind.OP_PLUS) {
				mv.visitInsn(FADD);
			}
			else if (expressionBinary.op == Kind.OP_MINUS) {
				mv.visitInsn(FSUB);
			}
			else if (expressionBinary.op == Kind.OP_TIMES) {
				mv.visitInsn(FMUL);
			}
			else if (expressionBinary.op == Kind.OP_DIV) {
				mv.visitInsn(FDIV);
			}
			else if (expressionBinary.op == Kind.OP_POWER) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else {
				throw new UnsupportedOperationException();
			}
		}
		else {
			throw new UnsupportedOperationException();
		}
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionConditional.guard.visit(this, arg);
		Label l2 = new Label();
		mv.visitJumpInsn(IFEQ, l2);
		expressionConditional.trueExpression.visit(this, arg);
		Label l3 = new Label();
		mv.visitJumpInsn(GOTO, l3);
		mv.visitLabel(l2);
		//mv.visitFrame(Opcodes.F_FULL, 3, new Object[] {"[Ljava/lang/String;", Opcodes.TOP, Opcodes.INTEGER}, 0, new Object[] {});
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitLabel(l3);
		//mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		switch (expressionFunctionAppWithExpressionArg.function) {
			case KW_abs:
				if (expressionFunctionAppWithExpressionArg.e.type == Type.FLOAT) {
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
				}
				else {
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
				}
				
				break;
			case KW_red:
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getRed", RuntimePixelOps.getRedSig, false);
				break;
			case KW_green:
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", RuntimePixelOps.getGreenSig, false);
				break;
			case KW_blue:
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getBlue", RuntimePixelOps.getBlueSig, false);
				break;
			case KW_alpha:
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getAlpha", RuntimePixelOps.getAlphaSig, false);
				break;
			case KW_sin:
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
				mv.visitInsn(D2F);
				break;
			case KW_cos:
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
				mv.visitInsn(D2F);
				break;
			case KW_atan:
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
				mv.visitInsn(D2F);
				break;
			case KW_log:
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
				mv.visitInsn(D2F);
				break;
			case KW_width:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig, false);
				break;
			case KW_height:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight", RuntimeImageSupport.getHeightSig, false);
				break;
			case KW_float:
				if (expressionFunctionAppWithExpressionArg.e.getType() == Type.INTEGER) {
					mv.visitInsn(I2F);
				}
				
				break;
			case KW_int:
				if (expressionFunctionAppWithExpressionArg.e.getType() == Type.FLOAT) {
					mv.visitInsn(F2I);
				}
				
				break;
			default:
				throw new UnsupportedOperationException();
		}
		
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		switch(expressionFunctionAppWithPixel.name) {
		case KW_cart_x:
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(F2D);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
			break;
		case KW_cart_y:
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(F2D);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
			break;
		case KW_polar_r:
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(I2D);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(I2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
			mv.visitInsn(D2F);
			break;
		case KW_polar_a:
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(I2D);
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(I2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
			mv.visitInsn(D2F);
			break;
		default:
			throw new UnsupportedOperationException();
		}

		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		switch (expressionIdent.getType()) {
			case INTEGER:
				mv.visitVarInsn(ILOAD, expressionIdent.dec.slotNumber);
				break;
			case FLOAT:
				mv.visitVarInsn(FLOAD, expressionIdent.dec.slotNumber);
				break;
			case BOOLEAN:
				mv.visitVarInsn(ILOAD, expressionIdent.dec.slotNumber);
				break;
			case FILE:
				mv.visitVarInsn(ALOAD, expressionIdent.dec.slotNumber);
				break;
			case IMAGE:
				mv.visitVarInsn(ALOAD, expressionIdent.dec.slotNumber);
				break;
			default:
				throw new UnsupportedOperationException();
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, expressionPixel.dec.slotNumber);
		expressionPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", RuntimeImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel", RuntimePixelOps.makePixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		switch (expressionPredefinedName.name) {
			case KW_Z:
				mv.visitIntInsn(SIPUSH, this.Z);
				break;
			case KW_default_width:
				mv.visitIntInsn(SIPUSH, this.defaultWidth);
				break;
			case KW_default_height:
				mv.visitIntInsn(SIPUSH, this.defaultHeight);
				break;
			default:
				throw new UnsupportedOperationException();
		}
		
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		switch (expressionUnary.op) {
			case OP_PLUS:
				// do nothing
				break;
			case OP_MINUS:
				if (expressionUnary.getType() == Type.INTEGER) {
					mv.visitInsn(INEG);
				}
				else if (expressionUnary.getType() == Type.FLOAT) {
					mv.visitInsn(FNEG);
				}
				
				break;
			case OP_EXCLAMATION:
				if (expressionUnary.getType() == Type.INTEGER) {
					mv.visitInsn(ICONST_M1);
					mv.visitInsn(IXOR);
				}
				else if (expressionUnary.getType() == Type.FLOAT) {
					// do nothing
				}
				else if (expressionUnary.getType() == Type.BOOLEAN) {
					Label l2 = new Label();
					mv.visitJumpInsn(IFEQ, l2);
					mv.visitInsn(ICONST_0);
					Label l3 = new Label();
					mv.visitJumpInsn(GOTO, l3);
					mv.visitLabel(l2);
					mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l3);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				}

				break;
			default:
				throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		switch (lhsIdent.type) {
			case INTEGER:
				mv.visitVarInsn(ISTORE, lhsIdent.dec.slotNumber);
				break;
			case FLOAT:
				mv.visitVarInsn(FSTORE, lhsIdent.dec.slotNumber);
				break;
			case BOOLEAN:
				mv.visitVarInsn(ISTORE, lhsIdent.dec.slotNumber);
				break;
			case FILE:
				mv.visitVarInsn(ASTORE, lhsIdent.dec.slotNumber);
				break;
			case IMAGE:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy", RuntimeImageSupport.deepCopySig, false);
				mv.visitVarInsn(ASTORE, lhsIdent.dec.slotNumber);
				break;
			default:
				throw new UnsupportedOperationException();
		}
		
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, lhsPixel.dec.slotNumber);
		lhsPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig, false);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, lhsSample.dec.slotNumber);
		lhsSample.pixelSelector.visit(this, arg);
		loadColor(lhsSample.color);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);
		return null;
	}

	public void loadColor(Kind color)
			throws Exception {
		switch (color) {
			case KW_alpha:
				mv.visitIntInsn(SIPUSH, RuntimePixelOps.ALPHA);
				break;
			case KW_red:
				mv.visitIntInsn(SIPUSH, RuntimePixelOps.RED);
				break;
			case KW_green:
				mv.visitIntInsn(SIPUSH, RuntimePixelOps.GREEN);
				break;
			case KW_blue:
				mv.visitIntInsn(SIPUSH, RuntimePixelOps.BLUE);
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		System.out.println("ex " + pixelSelector.ex.type);
		System.out.println("ey " + pixelSelector.ey.type);
		// TODO Auto-generated method stub
		if (pixelSelector.ex.type == Type.FLOAT || pixelSelector.ey.type == Type.FLOAT) {
			pixelSelector.ex.visit(this, arg);
			mv.visitInsn(F2D);
			pixelSelector.ey.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
			
			pixelSelector.ex.visit(this, arg);
			mv.visitInsn(F2D);
			pixelSelector.ey.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
		}
		else {
			pixelSelector.ex.visit(this, arg);
			pixelSelector.ey.visit(this, arg);
		}
		
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		//System.out.println("AAAAAAAAAAAAA");
		//System.out.println(arg);
		//System.out.println("AAAAAAAAAAAAA");
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		//mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeHeadless", "()V", false);
		
		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		statementIf.guard.visit(this, arg);
		Label l2 = new Label();
		mv.visitJumpInsn(IFEQ, l2);
		Label l3 = new Label();
		mv.visitLabel(l3);
		statementIf.b.visit(this, arg);
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
				
		switch (statementInput.dec.type) {
			case KW_int:
				mv.visitVarInsn(ALOAD, 0);
				statementInput.e.visit(this, arg);
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
				mv.visitVarInsn(ISTORE, statementInput.dec.slotNumber);
				break;
			case KW_float:
				mv.visitVarInsn(ALOAD, 0);
				statementInput.e.visit(this, arg);
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
				mv.visitVarInsn(FSTORE, statementInput.dec.slotNumber);
				break;
			case KW_boolean:
				mv.visitVarInsn(ALOAD, 0);
				statementInput.e.visit(this, arg);
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
				mv.visitVarInsn(ISTORE, statementInput.dec.slotNumber);
				break;
			case KW_image:
				mv.visitVarInsn(ALOAD, 0);
				statementInput.e.visit(this, arg);
				mv.visitInsn(AALOAD);
				// System.out.println(statementInput.dec.name + " " + statementInput.dec.height);
				if (statementInput.dec.width != null && statementInput.dec.height != null) {
					mv.visitVarInsn(ALOAD, statementInput.dec.slotNumber);
					mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig, false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitVarInsn(ALOAD, statementInput.dec.slotNumber);
					mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight", RuntimeImageSupport.getHeightSig, false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}
				else {
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ACONST_NULL);
				}
				
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage", RuntimeImageSupport.readImageSig, false);
				mv.visitVarInsn(ASTORE, statementInput.dec.slotNumber);
				break;
			case KW_filename:
				mv.visitVarInsn(ALOAD, 0);
				statementInput.e.visit(this, arg);
				mv.visitInsn(AALOAD);
				mv.visitVarInsn(ASTORE, statementInput.dec.slotNumber);
				break;
			default:
				throw new UnsupportedOperationException();
		}
		
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();;
		switch (type) {
			case INTEGER : {
					CodeGenUtils.genLogTOS(GRADE, mv, type);
					mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
							"Ljava/io/PrintStream;");
					mv.visitInsn(Opcodes.SWAP);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
							"println", "(I)V", false);
				}
				break;
			case BOOLEAN : {
					CodeGenUtils.genLogTOS(GRADE, mv, type);
					// TODO implement functionality
					mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
							"Ljava/io/PrintStream;");
					mv.visitInsn(Opcodes.SWAP);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
							"println", "(Z)V", false);
				}
				break; // commented out because currently unreachable. You will need
				// it.
			case FLOAT : {
					CodeGenUtils.genLogTOS(GRADE, mv, type);
					// TODO implement functionality
					mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
							"Ljava/io/PrintStream;");
					mv.visitInsn(Opcodes.SWAP);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
							"println", "(F)V", false);
				}
				break; // commented out because currently unreachable. You will need
				// it.
			case IMAGE : {
					CodeGenUtils.genLogTOS(GRADE, mv, type);
					// TODO implement functionality
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeFrame", "(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
					mv.visitInsn(POP);
					//mv.visitInsn(ICONST_1);
					//mv.visitMethodInsn(INVOKEVIRTUAL, "javax/swing/JFrame", "setVisible", "(Z)V", false);
				}
				break;
			default:
				throw new UnsupportedOperationException();
		}
		
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		statementSleep.duration.visit(this, arg);
		//System.out.println(statementSleep.duration);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
		statementWhile.b.visit(this, arg);
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		statementWhile.guard.visit(this, arg);
		mv.visitJumpInsn(IFNE, l3);
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		mv.visitVarInsn(ALOAD, statementWrite.sourceDec.slotNumber);
		mv.visitVarInsn(ALOAD, statementWrite.destDec.slotNumber);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", RuntimeImageSupport.writeSig, false);
		return null;
	}

}
