import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a b program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PreIncStmtNode     ExpNode
//       PreDecStmtNode     ExpNode
//       ReceiveStmtNode        ExpNode
//       PrintStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PreIncStmtNode, PreDecStmtNode, ReceiveStmtNode,   PrintStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  RepeatStmtNode,
//        CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// <<<ASTnode class (base class for all other kinds of nodes)>>>
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void addIndent(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++) p.print(" ");
    }
}

// **********************************************************************
// <<<ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode>>>
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    /*
    * Analyze method for ProgramNode creates a new symbol table for the base scope.
    */
    public void analyze() {
        SymTable table = new SymTable();
		myDeclList.analyze(table);
	}

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode");
            System.exit(-1);
        }
    }

    // Default analyze
    public void analyze(SymTable table) {
        this.analyze(table, table);
    }

    // If the node in our declList is of type VarDeclNode, we will want to pass
    // in the global table as well as the current table in our scope
	public void analyze(SymTable table, SymTable globalTab) {
		// Iterate through all the DeclNodes in the DeclList and run analysis
        for (DeclNode dn : myDecls) {
            if (dn instanceof VarDeclNode) {
                ((VarDeclNode)dn).analyze(table, globalTab);
            } else {
                dn.analyze(table);
            }
		}
	}

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // Run analysis on every FormalDeclNode in the FormalListNode
    public void analyze(SymTable table, FnSym sym) {
        for (FormalDeclNode fdl : myFormals) {
            fdl.analyze(table, sym);
        }
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // Analyze the DeclList in each FnBody
    // Then analyze the statement list
    public void analyze(SymTable table) {
        myDeclList.analyze(table);
        myStmtList.analyze(table);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // Analyze each statement in the statement list
    public void analyze(SymTable table){
        for (StmtNode sn : myStmts) {
            sn.analyze(table);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // Analyze each expression in the expression list
    public void analyze(SymTable table){
        for(ExpNode node : myExps){
            node.analyze(table);
        }
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// <<<DeclNode and its subclasses>>>
// **********************************************************************

abstract class DeclNode extends ASTnode {
	abstract public void analyze(SymTable table);
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(";");
    }

    // Need to have this to satisfy the abstract void analyze
    public void analyze(SymTable table) {
        this.analyze(table, table);
    }

    // Analyze the VarDecl
	public void analyze(SymTable table, SymTable globalTab) {
        Sym sym = null;
        IdNode struct = null;
        boolean multDecl = false;
        // Multiply Declared Check
        sym = table.lookupLocal(myId.toString());
        if (sym != null) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Multiply declared identifier");
            multDecl = true;
        }

        // void check
		if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Non-function declared void");
            return;
        }
        // if it is a struct check if its an invalid name
        else if (myType instanceof StructNode) {
            struct = ((StructNode)myType).getId();
            // use the globalTable to see if the structs name has been decalred
            sym = globalTab.lookupGlobal(struct.toString());
            // if the name isn't found, or it is but its not the right type, fatal
            if (sym == null || !(sym instanceof StructDefSym)) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Invalid name of struct type");
                return;
            }
        }
        
        // only be adding if its not multiply declared. but we still want to continue the other checks beforehand
        if (multDecl == false) {
            // if we get to this point, should be a good decl
            try {
                // check if its a struct
                if (myType instanceof StructNode) {
                    // if it is, create a new StructDeclSym
                    sym = new StructDeclSym((StructDefSym)(globalTab.lookupGlobal(struct.toString())), struct.toString());
                } else {
                    // if its not create a new regular sym
                    sym = new Sym(myType.getType());
                }
                // add it to our current symTable
                table.addDecl(myId.toString(), sym);
            } catch (DuplicateSymException e) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Unexpected DuplicateSymException in VarDeclNode analysis");
            } catch (EmptySymTableException e) {
                System.err.println("Unexpected EmptySymTableException in VarDeclNode analysis");
                System.exit(-1);
            } catch (WrongArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        }
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    public void analyze(SymTable table) {
        // Create a new FnSym that represents a function decl
        Sym sym = new FnSym(myType.getType());
        try {
            table.addDecl(myId.toString(), sym);
        } catch (DuplicateSymException e) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Multiply declared identifier");
        } catch (EmptySymTableException e) {
            System.err.println("Undefined Scope in FnDeclNode analysis");
            System.exit(-1);
        } catch (WrongArgumentException e){
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        // Add a scope to the current table (function body exists in its own scope)
        table.addScope();
        // analyze the formals list, provide the table as well as the corresponding symbol
        myFormalsList.analyze(table, (FnSym)sym);
        // finally, analyze the body of the funcion
        myBody.analyze(table);

        // then we will want to close that scope.
        try {
            table.removeScope();
        } catch (EmptySymTableException e) {
            System.err.println("No scope defined in FnDeclNode");
            System.exit(-1);
        }
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    // this isn't used
    public void analyze(SymTable table) {}

    public void analyze(SymTable table, FnSym fnSym) {
        // check if the type is of void
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Non-function declared void");
            return;
        } else {
            try {
                // this var doesnt get used, it is just to check to see if lookupLocal throws an exception
                Sym sym = table.lookupLocal(myId.toString());
                // create a new symbol
                sym = new Sym(myType.getType());
                // add it to the current scope
                table.addDecl(myId.toString(), sym);
                // add the formals to the function sym
                fnSym.addFormals(myType.getType());
            } catch (DuplicateSymException e) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Multiply declared identifier");
            } catch (EmptySymTableException e) {
                System.err.println("Undefined scope in FormalDeclNode");
                System.exit(-1);
            } catch (WrongArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        }
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("struct ");
        myId.unparse(p, 0);
        p.println("{");
        myDeclList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("};\n");

    }

    public void analyze(SymTable table) {
        try {
            // not used, just to look for duplicate sym
            Sym symCheck = table.lookupLocal(myId.toString());
            // create a whole new table for this struct table
            SymTable structTable = new SymTable();
            // analyze the declList in the struct
            myDeclList.analyze(structTable, table);
            // then create a new symbol for the struct def
            StructDefSym structDefSym = new StructDefSym(structTable, myId.toString());
            // and add it to the (global) symTable
            table.addDecl(myId.toString(), structDefSym);
        } catch (DuplicateSymException e) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Multiply declared identifier");
        } catch (EmptySymTableException e) {
            System.err.println("Undefined scope in StructDeclNode");
            System.exit(-1);
        } catch (WrongArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// <<<TypeNode and its Subclasses>>>
// **********************************************************************

abstract class TypeNode extends ASTnode {
	abstract public String getType();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }

	public String getType() {
		return "int";
	}
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }

	public String getType() {
		return "bool";
	}
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }

    public String getType() {
        return "void";
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        myId.unparse(p, 0);
    }

	public IdNode getId() {
		return myId;
	}

    public String getType() {
        return "struct";
    }
    
    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// <<<StmtNode and its subclasses>>>
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void analyze(SymTable table);
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    public void analyze(SymTable table){
        // analyze the assignmentNode
        myAssign.analyze(table);
    }

    // 1 kid
    private AssignNode myAssign;
}

class PreIncStmtNode extends StmtNode {
    public PreIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
	    p.print("++");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public void analyze(SymTable table){
        // analyze the expression
        myExp.analyze(table);
    }

    // 1 kid
    private ExpNode myExp;
}

class PreDecStmtNode extends StmtNode {
    public PreDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
	p.print("--");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public void analyze(SymTable table){
        // analyze the expression
        myExp.analyze(table);
    }

    // 1 kid
    private ExpNode myExp;
}

class ReceiveStmtNode extends StmtNode {
    public ReceiveStmtNode(ExpNode e) {
        myExp = e;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("receive >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public void analyze(SymTable table){
        // analyze the expression
        myExp.analyze(table);
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class PrintStmtNode extends StmtNode {
    public PrintStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("print << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public void analyze(SymTable table){
        // analyze the expression
        myExp.analyze(table);
    }

    // 1 kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");
    }

    public void analyze(SymTable table){
        // analyze the expression first
        myExp.analyze(table);
        // then add a scope
        table.addScope();
        // analyze the decls followed by the statements
        myDeclList.analyze(table);
        myStmtList.analyze(table);
        // finally remove the scope
        try{
            table.removeScope();
        } catch(EmptySymTableException ex){
            System.err.println("Undefined Scope in IfStmtNode analysis");
            System.exit(-1);
        }
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");
        addIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");        
    }

    public void analyze(SymTable table){
        // similar to the if, analyze the expression
        myExp.analyze(table);
        // add the scope
        table.addScope();
        // analyze the then declList and stmtList
        myThenDeclList.analyze(table);
        myThenStmtList.analyze(table);

        // close the scope
        try{
            table.removeScope();
        }catch(EmptySymTableException ex){
            System.err.println("Undefined Scope in IfElseStmtNode analysis");
            System.exit(-1);
        }

        // add a new scope for the else
        table.addScope();
        // analyze the declList and stmtList
        myElseDeclList.analyze(table);
        myElseStmtList.analyze(table);

        // close the scope
        try{
            table.removeScope();
        }catch(EmptySymTableException ex){
            System.err.println("Undefined Scope in IfElseStmtNode analysis");
            System.exit(-1);
        }
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");
    }

    public void analyze(SymTable table){
        // analyze the expression
        myExp.analyze(table);
        // add a scope
        table.addScope();
        // analyze the declList and stmtList
        myDeclList.analyze(table);
        myStmtList.analyze(table);
        // close the scope
        try{
            table.removeScope();
        } catch(EmptySymTableException ex) {
            System.err.println("Undefined Scope in WhileStmtNode analysis");
            System.exit(-1);
        }
    }
    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
	
    public void unparse(PrintWriter p, int indent) {
	    addIndent(p, indent);
        p.print("repeat (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");
    }

    public void analyze(SymTable table){
        // analyze the expression
        myExp.analyze(table);
        // open a new scope
        table.addScope();
        // analyze the declList and stmtList
        myDeclList.analyze(table);
        myStmtList.analyze(table);
        // close the scope
        try{
            table.removeScope();
        }catch(EmptySymTableException ex){
            System.err.println("Undefined Scope in RepeatStmtNode analysis");
            System.exit(-1);
        }
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    public void analyze(SymTable table){
        // analyze the call expression
        myCall.analyze(table);
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    public void analyze(SymTable table){
        // Check for null since it can be Null
        if(myExp != null){
            // if not, analyze the expression
            myExp.analyze(table);
        }
    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// <<<ExpNode and its subclasses>>>
// **********************************************************************

abstract class ExpNode extends ASTnode {
    // abstract public String toString();
    abstract public void analyze(SymTable table);
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    public String toString() {
        return myIntVal + "";
    }

    public void analyze(SymTable table){
        //Do nothing since members don't need analysis
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    public String toString() {
        return myStrVal;
    }

    public void analyze(SymTable table){
        //Do nothing since members don't need analysis
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    public String toString() {
        return "true";
    }

    public void analyze(SymTable table){
        //Do nothing since members don't need analysis
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    public String toString() {
        return "false";
    }

    public void analyze(SymTable table){
        //Do nothing since members don't need analysis
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {

    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (this.link != null) {
            // print out the sym name with each IdNode
            p.print("(" + link + ")");
            
        }
    }

	public String toString() {
		return myStrVal;
	}

    public int getLineNum() {
        return myLineNum;
    }

    public int getCharNum() {
        return myCharNum;
    } 

    public Sym getSym() {
        return link;
    }
    
    public void analyze(SymTable table) {
        // see if the symbol is in the table
        Sym foundSym = table.lookupGlobal(myStrVal);
        // if its not, its undeclared
        if (foundSym == null) {
            ErrMsg.fatal(this.myLineNum, this.myCharNum, "Undeclared identifier");
        } else {
            // otherwise link to it
            addLink(foundSym);
        }        
    }

    public void addLink(Sym linkSym) {
        this.link = linkSym;
    }

    public Sym getLink() {
        return this.link;
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
	private Sym link;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;    
        myId = id;
        prev = null;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print(").");
        myId.unparse(p, 0);
    }

    public String toString() {
        return ".";
    }

    public void analyze(SymTable table){
        // analyze on the LHS first
        myLoc.analyze(table);
        // declare a SymTable for a possible RHS dod-access
        SymTable structTable = null;
        // declare a sym used for checking
        Sym sym = null;
        // Check if myLoc is an IdNode, if it is then Idsym will be a link
        if (myLoc instanceof IdNode) {
            sym = ((IdNode)myLoc).getSym();
            // If it is null then return
            if (sym == null) {
                return;
            } else if (sym instanceof StructDeclSym) { 
                // if sym is a StructDeclSym, get the symTable for it
                structTable = ((StructDeclSym)sym).getBody().getTable();
            } else {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Dot-access of non-struct type");
                return;
            }
        } else if (myLoc instanceof DotAccessExpNode) {
            sym  = ((DotAccessExpNode)myLoc).getSym();
            // if there is no prev sym, then it was a bad access
            if (sym == null) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Dot-access of non-struct type");
                return;
            } else {
                // if myLoc is a StructDefSym
                if (sym instanceof StructDefSym) {
                    // get the corresponding table
                    structTable = ((StructDefSym)sym).getTable();
                } else {
                    // if it wasn't, throw a fatal message
                    ErrMsg.fatal(myId.getCharNum(), myId.getCharNum(), "Dot-access of non-struct type");
                    return;
                }
            }
        } else {
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }


        // If we get to this point in the code, then the symbol is valid
        sym = structTable.lookupGlobal(myId.toString());
        if (sym == null) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Invalid struct field name");
        } else {
            // Link the symbol
            myId.addLink(sym);
            // If the RHS is a struct, we want to do chained access
            if (sym instanceof StructDeclSym) {
                // store the previous sym
                prev = ((StructDeclSym)sym).getBody();
            } 
        }
    }

    public Sym getSym(){
        return prev;
    }
    public IdNode getId(){
        return myId;
    }
    // 4 kids
    private ExpNode myLoc;    
    private IdNode myId;
    private Sym prev;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }

    public String toString() {
        return "=";
    }
    
    public void analyze(SymTable table){
        // analyze the both expressions
        myLhs.analyze(table);
        myExp.analyze(table);
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // analyze the expression
    public void analyze(SymTable table){
        myId.analyze(table);
        if(myExpList != null){
            myExpList.analyze(table);
        }
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    public void analyze(SymTable table){
        // analyze the expression
        myExp.analyze(table);
    }
    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
    
    public void analyze(SymTable table){
        // analyze both expressions
        myExp1.analyze(table);
        myExp2.analyze(table);
    }
    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// <<<Subclasses of UnaryExpNode>>>
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// <<<Subclasses of BinaryExpNode>>>
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
