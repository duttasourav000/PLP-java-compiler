package cop5556sp18;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import cop5556sp18.AST.Declaration;

public class SymbolTable {
	class SymbolTableEntry {
		private String identifier;
		private Integer scopeNumber;
		private Declaration declaration;
		public SymbolTableEntry(
				String identifier,
				Integer scopeNumber,
				Declaration declaration) {
			this.setIdentifier(identifier);
			this.scopeNumber = scopeNumber;
			this.setDeclaration(declaration);
		}
		public String getIdentifier() {
			return identifier;
		}
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		public Declaration getDeclaration() {
			return declaration;
		}
		public void setDeclaration(Declaration declaration) {
			this.declaration = declaration;
		}
		public Integer getScopeNumber() {
			return this.scopeNumber;
		}
		public void setScopeNumber(Integer scopeNumber) {
			this.scopeNumber = scopeNumber;
		}
	}
	
	public SymbolTable() {
		this.currentScope = -1;
		this.nextScope = 0;
		this.scopeStack = new LinkedList<Integer>();
		this.symbolTable = new HashMap<String, LinkedList<SymbolTableEntry>>();
		this.enterScope();
	}
	
	int currentScope, nextScope;
	LinkedList<Integer> scopeStack;
	HashMap<String, LinkedList<SymbolTableEntry>> symbolTable;
	
	void enterScope() {
		this.currentScope = this.nextScope++;
		this.scopeStack.push(this.currentScope);
	}
	
	void closeScope() {
		this.scopeStack.pop();
		this.currentScope = this.scopeStack.getFirst();
	}
	
	void add(Declaration declaration) {
		// find if the entry is present get an index
		// else add an entry
		SymbolTableEntry entry = new SymbolTableEntry(declaration.name, this.currentScope, declaration);
		if (this.symbolTable.containsKey(declaration.name)) {
			this.symbolTable.get(declaration.name).push(entry);
		}
		else {
			this.symbolTable.put(declaration.name, new LinkedList<SymbolTableEntry>(Arrays.asList(entry)));
		}
	}
	
	Integer lookupScope(String identifier) {
		if (this.symbolTable.containsKey(identifier)) {
			for (SymbolTableEntry entry : this.symbolTable.get(identifier))
				for (Integer s : this.scopeStack) {
					if (s == entry.getScopeNumber()) {
						return entry.getScopeNumber();
					}
			}
		}
		
		return null;
	}
	
	Declaration lookup(String identifier) {
		if (this.symbolTable.containsKey(identifier)) {
			for (SymbolTableEntry entry : this.symbolTable.get(identifier))
				for (Integer s : this.scopeStack) {
					if (s == entry.getScopeNumber()) {
						return entry.getDeclaration();
					}
			}
		}
		
		return null;
	}
}
