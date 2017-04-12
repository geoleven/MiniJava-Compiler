import java.util.LinkedList;
import java.util.ListIterator;

import syntaxtree.*;
import visitor.GJDepthFirst;

public class MJVisitor extends GJDepthFirst<String, String> {
	public LinkedList<String> declaredClassList = new LinkedList<String>();		/*Declared classes*/
	public LinkedList<Var> allReachableVarsList = new LinkedList<Var>();
	public LinkedList<ClassIn> classl = new LinkedList<ClassIn>();
	public LinkedList<LinkedList<String>> formalsToBeChecked = new LinkedList<LinkedList<String>>();
	public int typeOfLastDeclaredType = -1;										/*Type of the last declared item*/
	public String ErrorMessages = "";
	public int NumberOfErrors = 0;
	public boolean FirstPass = true;
	public boolean ThirdPass = false;
	public int currScope = 0;
	public int prevScope = 0;
	public String lastTypeName;
	public int varc = 0;
	public int choiceOfLastStatement = -1;										/*Last picked statement*/
	public int typeOfLastExpression = -1;										/*Last expression type*/
	public int currentClassNo = -1;
	public ClassIn currentClassPtr = null;
	public Method currentMethod = null;
	public int choiceOfLastPrimaryExpression = -1;
	public boolean verbose = false;
	
	
	public ClassIn findclassptr(String name) {
		ListIterator<ClassIn> li = classl.listIterator();
		ClassIn temp;
		while (li.hasNext()) {
			temp = li.next();
			if (temp.name.equals(name))
				return temp;
		}
		return null;
	}
	
	public Method searchMethodName(LinkedList<Method> m, String name) {
		ListIterator<Method> li = m.listIterator();
		Method temp;
		while (li.hasNext()) {
			temp = li.next();
			if (temp.name.equals(name))
				return temp;
		}
		return null;
	}
	
	public void er(String msg) {
		ErrorMessages += msg;
		NumberOfErrors++;
	}
	
	public Var isDeclared(String name, LinkedList<Var> llv) {
		ListIterator<Var> li = llv.listIterator();
		Var temp;
		while (li.hasNext()) {
			temp = li.next();
			if (temp.name.equals(name))
				return temp;
		}
		return null;
	}
	
	public Var isGenDeclared(String name, ClassIn c, Method m) {
		if (verbose) {
			System.out.print("Going to search for variable: " + name);
			if (c!=null)
				System.out.print(" at class: " + c.name);
			if (m!=null)
				System.out.print(" and method: " + m.name);
			System.out.println(".");
		}
		ListIterator<Var> li;
		Var temp;
		if (m != null) {
			li= m.varl.listIterator();
			while (li.hasNext()) {
				temp = li.next();
				if (temp.name.equals(name))
					return temp;
			}
		}
		if (c != null) {
			li = c.varl.listIterator();
			while (li.hasNext()) {
				temp = li.next();
				if (temp.name.equals(name))
					return temp;
			}
		}
		if (verbose)
			System.out.println("Not found.");
		return null;
	}
	
	public boolean checkSameTypes(LinkedList<String> a, LinkedList<String> b) {
		if (a.size() != b.size()) {
			return false;
		}
		for (int counter = 0; counter < a.size(); counter++) {
			if (a.get(counter) != null && !a.get(counter).equals(b.get(counter))) {
				/*An to a den einai idios typos me to b vres tin klassi pou
				 * antikatoptrizei ton b. Oso i b exei uperklaseis, aneva mia klassi
				 * parapanw kai koita an einai idia me ton a*/
				ClassIn bc = findclassptr(b.get(counter));
				while(true) {					
					if (bc != null) {
						if (a.get(counter).equals(bc.name))
							//return true;
							break;
					}
					else {
						if (verbose) {
							for(String s :a){System.out.println(s);}
							System.out.println();
							for(String s :b){System.out.println(s);}
							System.out.print("__________________\n");
						}
						return false;
					}
					if (!bc.isBase)
						bc = bc.supclass;
					else {
						if (verbose) {
							for(String s :a){System.out.println(s);}
							System.out.println();
							for(String s :b){System.out.println(s);}
							System.out.print("__________________\n");
						}
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean tryOverwriteInheritedVar(Var old, String name, String type) {
		if (old.isInherited) {
			//if (old.type.equals(type))
				if (old.name.equals(name)) {
					old.isInherited = false;
					return true;
				}
		}
		return false;
	}
	
	public int copyVarList(int elements, LinkedList<Var> bvarl, LinkedList<Var> cvarl, boolean setInheritance) {
		String tempn;
		String tempt;
		int varc = 0;
		for (int counter = 0; counter < elements; counter++) {
			tempn = bvarl.get(counter).name;
			tempt = bvarl.get(counter).type;
			Var old = isDeclared(tempn, cvarl);
			if (old != null) {
//				cvarl.remove(old);
//				cvarl.push(new Var(tempn, tempt, false));
			}
			else {
				cvarl.push(new Var(tempn, tempt, setInheritance));
				varc++;
			}
		}
		return varc;
	}
	
	public boolean formalsAreSame(Method base, Method derived) {
		if (base.formaltl.size() != derived.formaltl.size() ) {
			return false;
		}
		for (int counter = 0; counter < base.formaltl.size(); counter++) {
			if (base.formaltl.get(counter) != derived.formaltl.get(counter))
				return false;
		}
		return true;
	}
	
	public int copyMethList(int elements, LinkedList<Method> bmethl, LinkedList<Method> cmethl, boolean setInheritance) {
		String tempn;
		String tempt;
		int methc = 0;
		for (int counter = 0; counter < elements; counter++) {
			tempn = bmethl.get(counter).name;
			tempt = bmethl.get(counter).type;
			Method old = searchMethodName(cmethl, tempn);
			if (old != null) {
//				cmethl.remove(old);
//				cmethl.push(new Method(tempn, tempt, false));
			}
			else {
				cmethl.push(new Method(tempn, tempt, setInheritance));
				cmethl.peek().varc = copyVarList(bmethl.get(counter).varc, bmethl.get(counter).varl, cmethl.peek().varl, false);
				methc++;
//				cmethl.peek().formaltl = new LinkedList<String>(bmethl.get(counter).formaltl);
//				cmethl.peek().formalnl = new LinkedList<String>(bmethl.get(counter).formalnl);
			}
		}
		return methc;
	}
	
	public boolean inheritPrevBaseClass(ClassIn child) {
		if (child.isBase)
			return false;
		ClassIn base;
		if ((base = child.supclass) == null) {
			System.out.println("Internal error @inheritPrevBaseClass(base class ptr)");
			return false;
		}
		child.varc = copyVarList(base.varc, base.varl, child.varl, true);
		child.metc = copyMethList(base.metc, base.methodl, child.methodl, true);
		return true;
	}

	public String visit(Goal n, String args) {
		/*Visitor makes three passes. First only parses classes and methods declaration, second parses var declaration and third parses statements*/
		
		/*First pass*/
		if(verbose)
			System.out.println("First pass.");
		
		/*Main class and formal argument*/
		classl.push(new ClassIn(n.f0.f1.f0.tokenImage));
		declaredClassList.push(n.f0.f1.f0.tokenImage);
		if (verbose) {
			System.out.println("Pushing class: " + n.f0.f1.f0.tokenImage);
		}
		classl.peek().methodl.push(new Method("main", "void"));
		(classl.peek().metc)++;
		Var temp = new Var(n.f0.f11.f0.tokenImage, "String[]");
		classl.peek().methodl.peek().varl.push(temp);
		(classl.peek().methodl.peek().varc)++;
		
		/*Rest of the classes*/
		n.f1.accept(this, n.f0.f1.f0.tokenImage);
		FirstPass = false;
		
		/*________________________________________________________________Second pass_________________________________________________________________________*/
		
		/*Second pass*/
		if(verbose)
			System.out.println("\nSecond pass.");
		/*Main class variables declaration phase*/
		if (classl.size() > 0)
			currentClassNo = classl.size()-1;
		else {
			er("Main not found. Line: " + n.f0.f6.endLine + ".\n");
			return ("\n\n" + NumberOfErrors + " errors encountered: \n" + ErrorMessages);
		}
		n.f0.accept(this, null);
		
		/*Other classes and methods' variables and formals declaration*/
		currentClassNo = classl.size()-2; 										/*Get the index of the class right after main*/
		if (classl.size() > 1)
			n.f1.accept(this, null);
		
		/*_________________________________________________________________Third pass_________________________________________________________________________*/
		
		/*Third pass*/
		ThirdPass = true;
		if(verbose)
			System.out.println("\nThird pass.");
		/*Statements and method returns*/
		currentClassNo = classl.size()-1;
		currentClassPtr = classl.get(currentClassNo);
		currentMethod = null;
		n.f0.accept(this, null);
		currentClassNo = classl.size()-2;
		if (classl.size() > 1) {
			currentClassPtr = classl.get(currentClassNo);
			currentMethod = null;
			n.f1.accept(this, null);
		}
		
		if (NumberOfErrors > 0)
			return ("\n\n" + NumberOfErrors + " errors encountered: \n" + ErrorMessages);
		else
			return ("Type check: ok.\n");
	}
	
	public String visit(MainClass n, String args) {
		if (!ThirdPass) {
			n.f14.accept(this, "Main");											/*VarDeclaration*/
		}
		else {
			/*Third pass, statements*/
			n.f15.accept(this, null);											/*Statement*/
		}
		return null;
	}

	public String visit(TypeDeclaration n, String args) {
		n.f0.accept(this, args);
		return null;
	}

	public String visit(ClassDeclaration n, String args) {
		if (FirstPass) {
			if (declaredClassList.contains(n.f1.f0.tokenImage) || n.f1.f0.tokenImage.equals(args)) {
				er("Double declaration of class. Line: " + n.f1.f0.endLine + ".\n");
			}
			else {
				classl.push(new ClassIn(n.f1.f0.tokenImage));
				if (verbose) {
					System.out.println("Pushing class: " + n.f1.f0.tokenImage);
				}
				declaredClassList.push(n.f1.f0.tokenImage);
				n.f4.accept(this, n.f1.f0.tokenImage);
			}
		}
		else if (ThirdPass) {
			if (currentClassNo >= 0)
				currentClassPtr = classl.get(currentClassNo);			
			if (verbose)
				System.out.println("Class: " + currentClassPtr.name);
			n.f4.accept(this, n.f1.f0.tokenImage);
			
			currentClassNo--;
		}
		else {
			if (currentClassNo >= 0)
				currentClassPtr = classl.get(currentClassNo);
			
			n.f3.accept(this, "BaseClassVarDeclaration");
			n.f4.accept(this, null);
			currentMethod = null;
			
			currentClassNo--;
		}
		return null;
	}

	public String visit(ClassExtendsDeclaration n, String args) {
		if (FirstPass) {
			if (declaredClassList.contains(n.f3.f0.tokenImage)) {
				if (declaredClassList.contains(n.f1.f0.tokenImage) || n.f1.f0.tokenImage.equals(args)) {
					er("Double declaration of class. Line: " + n.f1.f0.endLine + ".\n");
				}
				else {
					if (verbose) {
						System.out.println("Pushing class: " + n.f1.f0.tokenImage);
					}
					classl.push(new ClassIn(n.f1.f0.tokenImage, findclassptr(n.f3.f0.tokenImage)));
					declaredClassList.push(n.f1.f0.tokenImage);
					n.f6.accept(this, n.f3.f0.tokenImage);
				}
			}
			else {
				er("Trying to extend class that does not exist. Line: " + n.f1.f0.endLine + ".\n");
			}
		}
		else if (ThirdPass) {
			if (currentClassNo >= 0)
				currentClassPtr = classl.get(currentClassNo);
			
			if (verbose)
				System.out.println("Class: " + currentClassPtr.name);
			
			n.f6.accept(this, n.f3.f0.tokenImage);
			
			currentClassNo--;
		}
		else {
			if (currentClassNo >= 0)
				currentClassPtr = classl.get(currentClassNo);			
			if (!inheritPrevBaseClass(currentClassPtr)) {
				er("Unable to inherit super class variables and methods. Line: " + n.f3.f0.endLine + ".\n");
			}
			n.f5.accept(this, "SubClassVarDeclaration");
			n.f6.accept(this, null);
			currentMethod = null;
			
			currentClassNo--;
		}
		return null;
	}

	public String visit(VarDeclaration n, String args) {
		if (args == null) {
			n.f0.accept(this, null); 											/*Just set typeOfLastDeclaredType flag*/
			n.f1.accept(this, "VDI");
		}
		else if (args.equals("Main")) {
			n.f0.accept(this, args);
			n.f1.accept(this, "MainVarDeclName");
		}
		else if (args.equals("BaseClassVarDeclaration") || args.equals("SubClassVarDeclaration")) {
			String mtype = n.f0.accept(this, null);
			if (mtype != null) {
				String mname = n.f1.f0.tokenImage;
				Var temp = isDeclared(mname, currentClassPtr.varl);
				if (temp != null) {
					if (args.equals("SubClassVarDeclaration")) {
						if (!tryOverwriteInheritedVar(temp, mname, mtype))
							er("Variable name declared more than once or trying to overwrite inherited variable with different type variable. Line: " + n.f2.endLine + ".\n");
					}	
					else {
						if (verbose)
							System.out.println("Class: " + currentClassPtr.name + ", method: " + currentMethod.name + ", var: " + mname + ".\n");
						er("Variable name declared more than once. Line: " + n.f2.endLine + ".\n");
					}
				}
				else {
					if(verbose)
						System.out.println("Saving var: " + mname + " type: " + mtype + " for class: " + currentClassPtr.name + ".\n");
					Var vtemp = new Var(mname, mtype);
					currentClassPtr.varl.push(vtemp);
					currentClassPtr.varc++;
				}
			}
			else {
				er("Class type not declared. Line: " + n.f2.endLine + ".\n");
			}
		}
		else if (args.equals("MethodVariableDeclaration")) {
			if(verbose) {
				System.out.println("Declaring variables for class: " + currentClassPtr.name + " and method: " + currentMethod.name );
			}
			String mtype = n.f0.accept(this, null);
			if (mtype != null) {
				String mname = n.f1.f0.tokenImage;
				Var temp = isDeclared(mname, currentMethod.varl);
				if (temp != null) {
					er("Variable name declared more than once. Line: " + n.f2.endLine + ".\n");
				}
				else {
					Var vtemp = new Var(mname, mtype);
					currentMethod.varl.push(vtemp);
					currentMethod.varc++;
				}
			}
			else {
				er("Class type not declared. Line: " + n.f2.endLine + ".\n");
			}
		}
		else {

		}
		return null;
	}

	public String visit(MethodDeclaration n, String args) {
		if(FirstPass) {
			String mtype = n.f1.accept(this, null); 							/*Gets type literal from type(default), identifier*/
			if (mtype != null) {
				String mname = n.f2.f0.tokenImage;
				if (searchMethodName(classl.peek().methodl, mname) == null && !mname.equals("main")) {		//peek or getlast opws to ha????/
					classl.peek().methodl.push(new Method(mname, mtype));							//
					(classl.peek().metc)++;															// ta idia kai s ayta ta 2
				}
				else {
					er("Method name already exists for this class. Line: " + n.f2.f0.endLine + ".\n");
				}
			}
			else {
				er("Method type not correctly declared. Line: " + n.f2.f0.endLine + ".\n");
			}
		}
		else if (ThirdPass) {
			currentMethod = searchMethodName(currentClassPtr.methodl, n.f2.f0.tokenImage);
			
			if (verbose)
				System.out.println("Method: " + currentMethod.name);
			
			n.f8.accept(this, null);
			if (n.f10.accept(this, null) != currentMethod.type)
				er("Method return type is not the same as the method type. Line: " + n.f11.endLine + ".\n");
			
			currentMethod = null;
		}
		else {
			currentMethod = searchMethodName(currentClassPtr.methodl, n.f2.f0.tokenImage);
			
			if (verbose){
				System.out.println("Second pass, going to method variable declaration.");
			}
			
			n.f4.accept(this, null);											/*FormalParameterList*/
			if (!currentClassPtr.isBase) {
				Method pmeth = searchMethodName(currentClassPtr.supclass.methodl, n.f2.f0.tokenImage);
				if (pmeth != null)
					if (!formalsAreSame(pmeth, currentMethod))
						er("Derived class' method cannot have same name with different parameters Line: " + n.f2.f0.endLine + "..\n");
			}
			n.f7.accept(this, "MethodVariableDeclaration");
			
			currentMethod = null;
		}
		return null;
	}

//	public String visit(FormalParameterList n, String args) {
//		n.f0.accept(this, null);
//		n.f1.accept(this, null);
//		return null;
//	}

	public String visit(FormalParameter n, String args) {
		String mtype = n.f0.accept(this, null);
		if (mtype == null) {
			er("Formal parameter type not declared");
			return null;
		}
		String mname = n.f1.f0.tokenImage;
		currentMethod.formalnl.addLast(mname);
		currentMethod.formaltl.addLast(mtype);
		currentMethod.varl.push(new Var(mname, mtype));
		(currentMethod.varc)++;
		return null;
	}

//	public String visit(FormalParameterTail n, String args) {
//		n.f0.accept(this, null);
//		return null;
//	}
	
//	public String visit(FormalParameterTerm n, String args) {
//		n.f1.accept(this, null);
//		return null;
//	}

	public String visit(Type n, String args) {	
		typeOfLastDeclaredType = n.f0.which;
		if (args != null && args.equals("Main")) {
			n.f0.accept(this, "MainVarDeclType");
		}
		else {
			/*Default case, for Type, returns the literal of type*/
			return n.f0.accept(this, null);
		}
		return null;
	}

	public String visit(ArrayType n, String args) {
		return "int[]";
	}

	public String visit(BooleanType n, String args) {		
		return n.f0.tokenImage;
	}

	public String visit(IntegerType n, String args) {
		return n.f0.tokenImage;
	}

	public String visit(Statement n, String args) {
		choiceOfLastStatement = n.f0.which;
		n.f0.accept(this, null);
		return null;
	}

	public String visit(Block n, String args) {
		n.f1.accept(this, null);
		return null;
	}

	public String visit(AssignmentStatement n, String args) {
		String leftOperand = n.f0.f0.tokenImage;
		if (verbose)
			System.out.println("Left operand: " + leftOperand + ".");
		Var tempV = isGenDeclared(leftOperand, currentClassPtr, currentMethod);
		if (tempV != null) {
			if (verbose)
				System.out.println("\"" + leftOperand + "\" is a var of class \"" + currentClassPtr.name + "\" or method \"" + currentMethod.name + "\".");
			String leftOperanType = tempV.type;
			String rightOperandType = n.f2.accept(this, null);
			LinkedList<String> tll1 = new LinkedList<String>();
			tll1.push(leftOperanType);
			LinkedList<String> tll2 = new LinkedList<String>();
			tll2.push(rightOperandType);
			if (!checkSameTypes(tll1, tll2)) {
				if (rightOperandType == null)
					rightOperandType = "uknown";
				er("Assignment types do not match (" + leftOperanType + " - " + 
						rightOperandType + "). Identifier: " + leftOperand + ". Line: " + n.f3.endLine + ".\n");
			}
			else {
				//add code if needed				
			}
		}
		else {
			er("Trying to assign value to non declared variable: " + n.f0.f0.tokenImage + ". Line: " + n.f0.f0.endLine + ".\n");
		}
		choiceOfLastStatement = -1;
		return null;
	}

	public String visit(ArrayAssignmentStatement n, String args) {
		/*cuid [ exp1 ] = exp2 ;*/
		String cuid = n.f0.f0.tokenImage;
		Var tempV = isGenDeclared(cuid, currentClassPtr, currentMethod);
		if (tempV != null) {
			String cuidType = tempV.type;
			if (cuidType != null && cuidType.equals("int[]")) {
				String exp1Type = n.f2.accept(this, null);
				if (exp1Type != null && exp1Type.equals("int")) {
					String exp2Type = n.f5.accept(this, null);
					if (exp2Type == null || !exp2Type.equals("int")) {
						er("Trying to assign non integer expression to int array. Line: " + n.f0.f0.endLine + ".\n");
					}
//					else {
//						
//					}
				}
				else {
					er("Array index should only be an integer. Line: " + n.f3.endLine + ".\n");
				}
			}
			else {
				er("Array assignment should be used only on int arrays. Variable \"" + cuid + "\" is: " + cuidType + ". Line: " + n.f0.f0.endLine + ".\n");
			}
		}
		else {
			er("Trying to assign value to non declared variable. Line: " + n.f0.f0.endLine + ".\n");
		}
		choiceOfLastStatement = -1;
		return null;
	}

	public String visit(IfStatement n, String args) {
		/*if ( exp1 ) stat1 else stat2*/
		String exp1Type = n.f2.accept(this, null);
		if (exp1Type == null || !exp1Type.equals("boolean")) {
			er("Expression in if evaluation is not boolean. Line: " + n.f3.endLine + ".\n");
		}
		n.f4.accept(this, null);
		n.f6.accept(this, null);
		choiceOfLastStatement = -1;
		return null;
	}

	public String visit(WhileStatement n, String args) {
		/*while ( exp1 ) stat1*/
		String exp1Type = n.f2.accept(this, null);
		if (exp1Type == null || !exp1Type.equals("boolean")) {
			er("Expression in while evaluation is not boolean. Line: " + n.f3.endLine + ".\n");
		}
		n.f4.accept(this, null);
		choiceOfLastStatement = -1;
		return null;
	}

	public String visit(PrintStatement n, String args) {
		String expType = n.f2.accept(this, null);
		if (expType == null || !expType.equals("int")) {
			er("Cannot print non int type: " + expType + ". Line: " + n.f4.endLine + ".\n");
		}
		choiceOfLastStatement = -1;
		return null;
	}

	public String visit(Expression n, String args) {
		typeOfLastExpression = n.f0.which;
		return n.f0.accept(this, null);
	}

	public String visit(AndExpression n, String args) {
		/*cl1 && cl2*/
		String cl1Type = n.f0.accept(this, null);
		String cl2Type = n.f2.accept(this, null);
		if (cl1Type == null || cl2Type == null || !(cl1Type.equals("boolean") && cl2Type.equals("boolean"))) {
			er("Expressions before and after \"&&\" should be boolean. Line: " + n.f1.endLine + ".\n");
		}
		typeOfLastExpression = -1;
		return "boolean";
	}

	public String visit(CompareExpression n, String args) {
		/*pe1 < pe2*/
		String pe1Type = n.f0.accept(this, null);
		String pe2Type = n.f2.accept(this, null);
		if (pe1Type == null || pe2Type == null || !(pe1Type.equals("int") && pe2Type.equals("int"))) {
			er("Expressions before and after \"<\" should be integers. Line: " + n.f1.endLine + ".\n");
		}
		typeOfLastExpression = -1;
		return "boolean";
	}

	public String visit(PlusExpression n, String args) {
		/*pe1 + pe2*/
		String pe1Type = n.f0.accept(this, null);
		String pe2Type = n.f2.accept(this, null);
		if (pe1Type == null || pe2Type == null || !(pe1Type.equals("int") && pe2Type.equals("int"))) {
			er("Expressions before and after \"+\" should be integers. Line: " + n.f1.endLine + ".\n");
		}
		typeOfLastExpression = -1;
		return "int";
	}

	public String visit(MinusExpression n, String args) {
		/*pe1 - pe2*/
		String pe1Type = n.f0.accept(this, null);
		String pe2Type = n.f2.accept(this, null);
		if (pe1Type == null || pe2Type == null || !(pe1Type.equals("int") && pe2Type.equals("int"))) {
			er("Expressions before and after \"-\" should be integers. Line: " + n.f1.endLine + ".\n");
		}
		typeOfLastExpression = -1;
		return "int";
	}

	public String visit(TimesExpression n, String args) {
		/*pe1 * pe2*/
		String pe1Type = n.f0.accept(this, null);
		String pe2Type = n.f2.accept(this, null);
		if (pe1Type == null || pe2Type == null || !(pe1Type.equals("int") && pe2Type.equals("int"))) {
			er("Expressions before and after \"*\" should be integers. Line: " + n.f1.endLine + ".\n");
		}
		typeOfLastExpression = -1;
		return "int";
	}

	public String visit(ArrayLookup n, String args) {
		/*pe1 [ pe2 ]*/
		String pe1Type = n.f0.accept(this, null);
		String pe2Type = n.f2.accept(this, null);
		if (pe1Type == null || pe2Type == null || !(pe1Type.equals("int[]") && pe2Type.equals("int"))) {
			er("Array lookup only to be used on int[] variables with index of int type. Line: " + n.f3.endLine + ".\n");
		}
		typeOfLastExpression = -1;
		return "int";
	}

	public String visit(ArrayLength n, String args) {
		/*pe1 . length*/
		String pe1Type = n.f0.accept(this, null);
		if (pe1Type == null || !pe1Type.equals("int[]")) {
			er("Array length only to be used on int[] variables. Line: " + n.f2.endLine + ".\n");
		}
		typeOfLastExpression = -1;
		return "int";
	}

	
	public String visit(MessageSend n, String args) {
		/*pe . id ( exl? )*/
		String peType = n.f0.accept(this, null);
		if (peType != null) {
			ClassIn octemp = findclassptr(peType);
			if (octemp != null) {
				Method mtemp = searchMethodName(octemp.methodl, n.f2.f0.tokenImage);
				if (mtemp != null) {
					formalsToBeChecked.push(new LinkedList<String>());					
					n.f4.accept(this, null);
					if (mtemp.formaltl.size() == formalsToBeChecked.peek().size()) {
						if(checkSameTypes(mtemp.formaltl, formalsToBeChecked.peek())) {
							formalsToBeChecked.pop();
							return mtemp.type;
						}
						else {
							er("Method \"" + mtemp.name + "\" takes different type of arguments. Line: " + n.f5.endLine + ".\n");
						}
					}
					else {
						er("Method " + mtemp.name + " takes different number of arguments (takes: " + 
								mtemp.formaltl.size() + ", has: " + formalsToBeChecked.peek().size() + "). Line: " + n.f5.endLine + ".\n");
					}
					
					formalsToBeChecked.pop();
				}
				else {
					er("This class type does not have such a method. Line: " + n.f1.endLine + ".\n");
				}
			}
			else {
				er("Calling member of non declared class or class instance \"" + peType + "\". Line: " + n.f1.endLine + ".\n");
			}
		}
		else {
			er("Type of field member caller could not be found. Line: " + n.f5.endLine + ".\n");
		}
		return null;
	}

	public String visit(ExpressionList n, String args) {	
		formalsToBeChecked.peek().addLast(n.f0.accept(this, null));
		n.f1.accept(this, null);
		return null;
	}

	public String visit(ExpressionTail n, String args) {
		n.f0.accept(this, null);
		return null;
	}
	
	public String visit(ExpressionTerm n, String args) {
		formalsToBeChecked.peek().addLast(n.f1.accept(this, null));
		return null;
	}

	public String visit(Clause n, String args) {
		/*O elegxos gia to an einai boolean ginetai sto AndExpression*/
		typeOfLastExpression = -1;
		return n.f0.accept(this, null);
	}

	public String visit(PrimaryExpression n, String args) {
		choiceOfLastPrimaryExpression = n.f0.which;
		return n.f0.accept(this, "PrimaryExpression");
	}

	public String visit(IntegerLiteral n, String args) {
		return "int";
	}

	public String visit(TrueLiteral n, String args) {
		return "boolean";
	}

	public String visit(FalseLiteral n, String args) {
		return "boolean";
	}

	public String visit(Identifier n, String args) {
		if(verbose)
			System.out.println(n.f0.tokenImage + " type " + typeOfLastDeclaredType + " args: " + args);
		if (args == null) {														/*Classes name declaration*/
			if (FirstPass) {
				return n.f0.tokenImage;
			}
			else {
				if (typeOfLastDeclaredType == 3) {
					if (!declaredClassList.contains(n.f0.tokenImage)) {
						er("Class \"" + n.f0.tokenImage + "\" not declared. Line: " + n.f0.endLine + ".\n");
						return null;
					}
					else {
						return n.f0.tokenImage;
					}
				}
			}
		}
		else if (args.equals("VDI") || args.equals("MainVarDeclName")) {
			ClassIn myClass;
			if (args.equals("MainVarDeclName"))
				myClass = classl.getLast();
			else
				myClass = classl.getFirst();
			if (isDeclared(n.f0.tokenImage, myClass.varl) != null) {
				er("Type instance name already in use. Line: " + n.f0.endLine + ".\n");
			}
			else {
				Var temp;
				if(verbose)
					System.out.println(n.f0.tokenImage + " type " + typeOfLastDeclaredType);
				if (typeOfLastDeclaredType == 0) {
					temp = new Var(n.f0.tokenImage, "int[]");
				}
				else if (typeOfLastDeclaredType == 1) {
					temp = new Var(n.f0.tokenImage, "boolean");
				}
				else if (typeOfLastDeclaredType == 2) {
					temp = new Var(n.f0.tokenImage, "int");
				}
				else if (typeOfLastDeclaredType == 3) {
					temp = new Var(n.f0.tokenImage, lastTypeName);
					lastTypeName = "";
				}
				else {
					temp = new Var(n.f0.tokenImage, "Error");
					er("Something weird happened with some type declaration. Line: " + n.f0.endLine + ".\n");
				}
				myClass.varl.push(temp);
				(myClass.varc)++;
			}
			typeOfLastDeclaredType = -1;
		}
		else if (args.equals("MainVarDeclType")) {
			if (typeOfLastDeclaredType == 3) {
				if (!declaredClassList.contains(n.f0.tokenImage)) {
					er("Cannot make an instance of a non declared type. Line: " + n.f0.endLine + ".\n");
				}
				else {
					lastTypeName = n.f0.tokenImage;
				}
			}
		}
		else if (args.equals("PrimaryExpression")) {
			Var vtemp = isGenDeclared(n.f0.tokenImage, currentClassPtr, currentMethod);
			if (vtemp != null) {
				return vtemp.type; 
			}
			ClassIn ctemp = findclassptr(n.f0.tokenImage);
			if (ctemp != null) {
				return ctemp.name;
			}
		}
		else {
			
		}
		return null;
	}

	public String visit(ThisExpression n, String args) {
		if (currentClassPtr == null) {
			er("This expression while not in statements. Line: " + n.f0.endLine + ".\n");
			return null;
		}
		else
			return currentClassPtr.name;
	}

	public String visit(ArrayAllocationExpression n, String args) {
		/*new int [ exp ]*/
		if (!n.f3.accept(this, null).equals("int")) {
			er("Size of int[] must be an integer. Line: " + n.f4.endLine + ".\n");
		}
		return "int[]";
	}

	public String visit(AllocationExpression n, String args) {
		if (declaredClassList.contains(n.f1.f0.tokenImage)) {
			return n.f1.f0.tokenImage;
		}
		else {
			er("Cannot make instance of not declared type. Line: " + n.f1.f0.endLine + ".\n");
			return null;
		}
	}

	public String visit(NotExpression n, String args) {
		return n.f1.accept(this, null);
	}

	public String visit(BracketExpression n, String args) {
		return n.f1.accept(this, null);
	}

}
