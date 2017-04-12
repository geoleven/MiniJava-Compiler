import java.util.LinkedList;
import java.util.ListIterator;

import syntaxtree.*;
import visitor.GJDepthFirst;

class StructData {
	String sType = null;
	String arrLnTemp = null;
	public StructData(String mytype) { sType = mytype; }
	public void setArrLeng(String myLeng) { arrLnTemp = myLeng; }
	public String getArrLeng() { return arrLnTemp; }
}

public class MJSPGVisitor extends GJDepthFirst<String, String> {
	
	public MJVisitor prevVis = null;
	String OutPutBuffer = "";
	int tfpln = 0;
	int nextTemp = 110;
	int nextLabel = 20;
	int currentFrameTemps = 0;
	String lastArrayLength = null;
	String lastExprListTemps = "";
	LinkedList<Integer> tempNums = new LinkedList<Integer>();
	LinkedList<String> tempIdent = new LinkedList<String>();
	LinkedList<StructData> tempData = new LinkedList<StructData>();
	LinkedList<Integer>	tempRecycle = new LinkedList<Integer>();
	LinkedList<Integer> labels = new LinkedList<Integer>();
	String currentThisType = "void";
	
	public String visit(Goal n, String args) {
		if (prevVis == null) {
			System.err.println("No type checking visitor given!");
			return null;
		}
		n.f0.accept(this, null);
		n.f1.accept(this, null);
		return OutPutBuffer;
	}
	
	public String visit(MainClass n, String args) {
		emit("MAIN\n");
		//n.f14.accept(this, n.f1.f0.tokenImage);
		ClassIn curCl = prevVis.findclassptr(n.f1.f0.tokenImage);
		curCl.varl.forEach(v -> {
			newTemp(v.name, v.type);
		});
		Method curMth = prevVis.searchMethodName(curCl.methodl, "main");
		curMth.varl.forEach(v -> {
			newTemp(v.name, v.type);
		});
		n.f15.accept(this, null);
		emit("END\n");
		return null;
	}

//	public String visit(TypeDeclaration n, String args) {
//		n.f0.accept(this, args);
//		return null;
//	}

	public String visit(ClassDeclaration n, String args) {
		currentThisType = n.f1.f0.tokenImage;
		//n.f3.accept(this, n.f1.f0.tokenImage);
		
		ClassIn curCl = prevVis.findclassptr(n.f1.f0.tokenImage);
		curCl.varl.forEach(v -> {
			newTemp(v.name, v.type);
		});
		
		
		n.f4.accept(this, n.f1.f0.tokenImage);
		currentThisType = "void";
		return null;
	}

	public String visit(ClassExtendsDeclaration n, String args) {
		currentThisType = n.f1.f0.tokenImage;
		//pass other methods vars from prev type check
		//n.f5.accept(this, n.f1.f0.tokenImage);
		
		ClassIn curCl = prevVis.findclassptr(n.f1.f0.tokenImage);
		curCl.varl.forEach(v -> {
			newTemp(v.name, v.type);
		});
		
		n.f6.accept(this, n.f1.f0.tokenImage);
		currentThisType = "void";
		return null;
	}

//	public String visit(VarDeclaration n, String args) {
//		newTemp(n.f1.f0.tokenImage, n.f0.accept(this, "getName"));
//		return null;
//	}

	public String visit(MethodDeclaration n, String args) {
		//currentFrameTemps++;
		String permRes = newTemp("_", n.f1.accept(this, "getName"));
		int previousFrameTemps = currentFrameTemps;
		currentFrameTemps = 0;
		tfpln = 0;
		n.f4.accept(this, null);
		int fpln = tfpln + 1;
		tfpln = 0;
		emit(args + "_" + n.f2.f0.tokenImage + " [" + Integer.toString(fpln) + "]\nBEGIN\n");
		
		//n.f7.accept(this, null); 
		
		ClassIn curCl = prevVis.findclassptr(args);
		Method curMth = prevVis.searchMethodName(curCl.methodl, n.f2.f0.tokenImage);
		curMth.varl.forEach(v -> {
			newTemp(v.name, v.type);
		});
		
		for(int counter = 1; counter < fpln; counter++) {
			int position = curMth.varl.size() - counter;
			String existingTempOfFofmal = toTemp(curMth.varl.get(position).name);
			//System.err.println(curMth.varl.get(position).name);
			emit("\tMOVE " + existingTempOfFofmal + " TEMP " + Integer.toString(counter) + "\n");
		}
		
		
		n.f8.accept(this, null);
		
		//String tempRes = n.f10.accept(this, n.f1.accept(this, "getName"));
		String tempRes = n.f10.accept(this, null);
		
				
		emit("\tMOVE " + permRes + " " + tempRes + "\n");
		
		emit("RETURN\n"); 
		emit("\t" + permRes + "\n");
		emit("END\n");
		removeFrame();
		
		if (currentFrameTemps != 0)
			System.err.println("Frame removal left uncleaned objects: " + Integer.toString(currentFrameTemps) + ".");
		currentFrameTemps = previousFrameTemps;
		return null;
	}

	public String visit(FormalParameter n, String args) {
		tfpln = tfpln + 1;
		//newTemp(n.f1.f0.tokenImage, n.f0.accept(this, "getName"));
		//String permaTempOfPar = 
		return null;
	}

	public String visit(Type n, String args) {
			return n.f0.accept(this, args);
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
		n.f0.accept(this, args);
		return null;
	}

	public String visit(Block n, String args) {
		n.f1.accept(this, args);
		return null;
	}

	public String visit(AssignmentStatement n, String args) {
		String prevLen = lastArrayLength;
		
		String assignedExp = n.f2.accept(this, null);
		String permTempOfIdent = toTemp(n.f0.f0.tokenImage);
		if (permTempOfIdent == null) {
			System.err.println("Identifier doesn't exist yet.(@AssignmentStatement)");
			return null;
		}
		emit("\tMOVE " + permTempOfIdent + " " + assignedExp + "\n");
		if (lastArrayLength != null)
			if (!setLength(n.f0.f0.tokenImage, lastArrayLength))
				System.err.println("Error while pushing array length to linked list.");
		lastArrayLength = prevLen;
		//discard(assignedExp, false);
		return null;
	}

	public String visit(ArrayAssignmentStatement n, String args) {		
		String sizeNormal = n.f2.accept(this, null);							//offset
		String assignTemp = n.f5.accept(this, null); 							//get the TEMP for the right operand
		
		String sizeOff = newTemp(null, "int");									//offset * 4
		String arrayAddress = toTemp(n.f0.f0.tokenImage);						//The address of the array
		if (arrayAddress == null) {
			System.err.println("Identifier of array doesn't exist yet.(@ArrayAssignmentStatement)");
			return null;
		}
		String arrayPlusOff = newTemp(null, "ptr"); 							//The address of the array plus the offset
		
		emit("\tMOVE " + sizeOff + " TIMES " + sizeNormal + " 4\n");			//sizePlusOff = 4*size
		emit("\tMOVE " + arrayPlusOff + " PLUS " + arrayAddress + " " + sizeOff + "\n"); //arrayPlusOff = address of the array plus sizePlusOff
		emit("\tHSTORE " + arrayPlusOff + " 0 " + assignTemp + "\n");				//store to the arrayPlusOff the TEMP from the right operand.
		
		//discard(sizeNormal, false);
		//discard(assignTemp, false);
		//discard(sizeOff, false);
		//discard(arrayPlusOff, false);
		
		return null;
	}

	public String visit(IfStatement n, String args) {
		String condition = n.f2.accept(this, null);
		String elseLabel = newLabel();
		String endIfLabel = newLabel();
		
		emit("\tCJUMP " + condition + " " + elseLabel + "\n");					//If condition is 1 do if code else go to else label
		n.f4.accept(this, null);												//Do if statement code
		emit("\tJUMP " + endIfLabel + "\n");										//Skip else statement code
		
		emit(elseLabel + "\tNOOP\n");											//else label
		n.f6.accept(this, null);												//Do else statement code
		emit(endIfLabel + "\tNOOP\n");											//end if (useful when condition is true)
		
		//discard(condition, false);
		return null;
	}

	public String visit(WhileStatement n, String args) {
		
		String condLabel = newLabel();
		String whileEnd = newLabel(); 
		
		emit(condLabel + "\tNOOP\n");											//Condition label (place to come to repeat loop)
		String condition = n.f2.accept(this, null);								//Calculate expression
		emit("\tCJUMP " + condition + " " + whileEnd + "\n");						//If condition is 1 loop else go after loop
		n.f4.accept(this, null);												//Do loop code
		emit("\tJUMP " + condLabel + "\n");										//Repeat condition check e.t.c.
		emit(whileEnd + "\tNOOP\n");												//Place to come if condition is not met
		
		
		//discard(condition, false);
		return null;
	}

	public String visit(PrintStatement n, String args) {
		String printTemp = n.f2.accept(this, args);
		emit("\tPRINT " + printTemp + "\n"); 
		return null;
	}

	public String visit(Expression n, String args) {
		return n.f0.accept(this, args);
	}

	public String visit(AndExpression n, String args) {
		String andTemp = newTemp(null, "boolean");
		String fCl = n.f0.accept(this, null);
		String sCl = n.f2.accept(this, null);
		String endLabel = newLabel();
		String falseLabel = newLabel();

		emit("\tCJUMP " + fCl + " " + falseLabel + "\n");
		emit("\tCJUMP " + sCl + " " + falseLabel + "\n");
		emit("\tMOVE " + andTemp + " 1\n");
		emit("\tJUMP " + endLabel + "\n");
		emit(falseLabel + "\tNOOP\n");
		emit("\tMOVE " + andTemp + " 0\n");
		emit(endLabel + "\tNOOP\n");
		
		//discard(fCl, false);
		//discard(sCl, false);
		return andTemp;
	}

	public String visit(CompareExpression n, String args) {
		String cmpTemp = newTemp(null, "boolean");
		
		String lExp = n.f0.accept(this, null);
		String rExp = n.f2.accept(this, null);
		
		emit("\tMOVE " + cmpTemp + " LT "+ lExp + " " + rExp + "\n");
		
		//discard(lExp, false);
		//discard(rExp, false);
		return cmpTemp;
	}

	public String visit(PlusExpression n, String args) {
		String plusTemp = newTemp(null, "int");
		
		String lOp = n.f0.accept(this, null);
		String rOp = n.f2.accept(this, null);
		
		emit("\tMOVE " + plusTemp + " PLUS " + lOp + " " + rOp + "\n");
		
		//discard(lOp, false);
		//discard(rOp, false);
		return plusTemp;
	}

	public String visit(MinusExpression n, String args) {
		String minusTemp = newTemp(null, "int");
		
		String lOp = n.f0.accept(this, null);
		String rOp = n.f2.accept(this, null);
		emit("\tMOVE " + minusTemp + " MINUS " + lOp + " " + rOp + "\n");
		
		//discard(lOp, false);
		//discard(rOp, false);
		return minusTemp;
	}

	public String visit(TimesExpression n, String args) {
		String timesTemp = newTemp(null, "int");
		
		String lOp = n.f0.accept(this, null);
		String rOp = n.f2.accept(this, null);
		
		emit("\tMOVE " + timesTemp + " TIMES " + lOp + " " + rOp + "\n");
		
		//discard(lOp, false);
		//discard(rOp, false);
		return timesTemp;
	}
	
	public String visit(ArrayLookup n, String args) {
		String sizeOffset = newTemp(null, "int");
		String addressPlusOff = newTemp(null, "ptr");
		String returnInt = newTemp(null, "int");
		
		String sizeNormal = n.f2.accept(this, null);
		String arrayAddress = n.f0.accept(this, null);
		
		emit("\tMOVE " + sizeOffset + " TIMES " + sizeNormal + " 4\n");
		emit("\tMOVE " + addressPlusOff + " PLUS " + sizeOffset + " " + arrayAddress + "\n");
		emit("\tHLOAD " + returnInt + " " + addressPlusOff + " 0\n");
		
		//discard(sizeOffset, false);
		//discard(addressPlusOff, false);
		return returnInt;
	}

	public String visit(ArrayLength n, String args) {
		String tempLen = getLength(n.f0.accept(this, "getName"));
		String lenExp = newTemp(null, "int");		
		emit("\tMOVE " + lenExp + tempLen + "\n");
		return lenExp;
	}

	
	public String visit(MessageSend n, String args) {
		String prevExprList = lastExprListTemps;
				
		String callerObjPtr  = n.f0.accept(this, null);
		String callerType = getTypeFromTemp(callerObjPtr);
				
		String methodAddrPtr = getVar(callerObjPtr, callerType, true , n.f2.f0.tokenImage); 
		String callRes = newTemp(null, getTypeFromTemp(methodAddrPtr));
				
				
		
		
		String prevThis = currentThisType;
		currentThisType = callerType;
		//System.err.println("\n\n" + callerType);
		//System.err.println(n.f2.f0.tokenImage);
		
		n.f4.accept(this, null);
		
		String methodAddr = newTemp(null, getTypeFromTemp(methodAddrPtr));
		emit("\tHLOAD " + methodAddr + " " + methodAddrPtr + " 0\n");
		
		emit("\tMOVE " + callRes + " CALL " + methodAddr + " ( " + callerObjPtr + " " + lastExprListTemps + " )\n");
		currentThisType = prevThis;
		
		lastExprListTemps = prevExprList;
		return callRes;
	}

	public String visit(ExpressionList n, String args) {		
		lastExprListTemps = "";
		lastExprListTemps = n.f0.accept(this, args);		
		
		if (n.f1.f0.present()) {
			lastExprListTemps = lastExprListTemps + n.f1.accept(this, args);
		}
		
		return null;
	}

	public String visit(ExpressionTail n, String args) {
		String result = "";
		for (int counter = 0; counter < n.f0.size(); counter++) {
			result = result + n.f0.elementAt(counter).accept(this, null);
		}
		return result;
	}
	
	public String visit(ExpressionTerm n, String args) {
		String result = n.f1.accept(this, args);
		if (!result.substring(0, 4).equals("TEMP")) {
			System.err.println("Expression pushed non TEMP value.");
		}
		return(" " + result);
	}

	public String visit(Clause n, String args) {
		return n.f0.accept(this, args);
	}

	public String visit(PrimaryExpression n, String args) {
		return n.f0.accept(this, args);
	}

	public String visit(IntegerLiteral n, String args) {
		String retInt = newTemp(null, "int");
		emit("\tMOVE " + retInt + " " + n.f0.tokenImage + "\n");
		return retInt;
	}

	public String visit(TrueLiteral n, String args) {
		String retBool = newTemp(null, "boolean");
		emit("\tMOVE " + retBool + " " + "1\n");
		return retBool;
	}

	public String visit(FalseLiteral n, String args) {
		String retBool = newTemp(null, "boolean");
		emit("\tMOVE " + retBool + " " + "1\n");
		return retBool;
	}

	public String visit(Identifier n, String args) {
		if (args != null && args.equals("getName"))
			return n.f0.tokenImage;
		else {
			String tempad = toTemp(n.f0.tokenImage);
			if (tempad == null) {
				System.err.println("Tried to get temp of not initialized obj. Trying to recover.");
				return n.f0.tokenImage;
			}
			return tempad;
		}
	}

	public String visit(ThisExpression n, String args) {
		//String tempThis = newTemp(null, "ptr");
		String tempThis = newTemp("this", getThisType());
		emit("\tMOVE " + tempThis + " TEMP 0\n");
//		String thisObj = newTemp(null, getThisType());
//		emit("\tHLOAD " + thisObj + " " + tempThis + " 0\n");
		return tempThis;
	}

	public String visit(ArrayAllocationExpression n, String args) {	
		String sizeOff = newTemp(null, "int");									//size*4
		String arrAdrs = newTemp(null, "int[]");								//address of int seq
		
		String sizeNor = n.f3.accept(this, null);
		
		lastArrayLength = sizeNor;
		//System.err.println(sizeNor);
		
		emit("\tMOVE " + sizeOff + " TIMES " + sizeNor + " 4\n");
		emit("\tMOVE " + arrAdrs + " HALLOCATE " + sizeOff + "\n");
		emit("\tMOVE TEMP 100 0\n");
		
		
		//Initialization
		String initSrt = newLabel();
		String initEnd = newLabel();
		String curAddr = newTemp(null, "int");
		String tpmAddr = newTemp(null, "int");
		String condJmp = newTemp(null, "boolean");
		String arrEnd  = newTemp(null, "int");
		
		emit("\tMOVE " + arrEnd  + " PLUS " + arrAdrs + " " + sizeOff + "\n");
		emit("\tMOVE " + curAddr + " " + arrAdrs + "\n");
		
		
		emit(initSrt + "\tNOOP\n");
		emit("\tHSTORE " + curAddr + " 0 TEMP 100\n");
		emit("\tMOVE " + tpmAddr + " PLUS " + curAddr + " 4\n");
		emit("\tMOVE " + curAddr + " " + tpmAddr + "\n");
		emit("\tMOVE " + condJmp + " LT " + curAddr + " " + arrEnd + "\n");
		emit("\tCJUMP " + condJmp + " " + initEnd + "\n");
		emit("\tJUMP " + initSrt + "\n");
		emit(initEnd + "\tNOOP\n");
		
		return arrAdrs;
	}

	public String visit(AllocationExpression n, String args) {
		int size = getClassAllocationSize(n.f1.f0.tokenImage);
		String objVal = newTemp(args, n.f1.f0.tokenImage);
		emit("\tMOVE " + objVal + " HALLOCATE " + size + "\n");
//		for (int counter = 0; counter < (size/4); counter++) {
//			emit("\tHSTORE " + objVal + " " + Integer.toString(counter*4) + " 0\n");
//		}
		String objPtr = newTemp(null, n.f1.f0.tokenImage);// + "*");
		emit("\tMOVE " + objPtr + " HALLOCATE " + Integer.toString(8) + "\n");
		emit("\tMOVE TEMP 100 0\n");
		
		//for (int counter = 0; counter < 12/4; counter++) {
			emit("\tHSTORE " + objPtr + " " + Integer.toString(0 /*counter*4*/ ) + " TEMP 100\n");
			emit("\tHSTORE " + objPtr + " " + Integer.toString(4 /*counter*4*/ ) + " TEMP 100\n");
		//}
		ClassIn curClass = prevVis.findclassptr(n.f1.f0.tokenImage);
		//Initialize to zero the Var fields
		for (int counter = 0; counter < curClass.varc; counter++) {
			emit("\tHSTORE " + objVal + " " + Integer.toString(counter*4) + " TEMP 100\n");
		}
		int[] offset = {0};
		offset[0] = curClass.varc * 4;
		curClass.methodl.forEach( m -> {
			String methName = newTemp(m.name, m.type);
			if (m.isInherited)
				emit("\tMOVE " + methName + " " + curClass.supclass.name + "_" + m.name + "\n");
			else
				emit("\tMOVE " + methName + " " + curClass.name + "_" + m.name + "\n");
			emit("\tHSTORE " + objVal + " " + Integer.toString(offset[0]) + " " + methName + "\n");
			offset[0] = offset[0] + 4;
		});
		emit("\tHSTORE " + objPtr + " " + Integer.toString(0) + " " + objVal + "\n");
		return objPtr;
	}

	public String visit(NotExpression n, String args) {
		String newBool = newTemp(null, "boolena");
		
		String prevBool = n.f1.accept(this, args);
		
		emit("\tMOVE " + newBool + " LT " + prevBool + " 1\n");
		
		//discard(prevBool, false);
		return newBool;
	}

	public String visit(BracketExpression n, String args) {
		return n.f1.accept(this, args);
	}
	
	String newTemp(String mname, String args) {
		int temp;
		if (tempRecycle.isEmpty()) {
			temp = nextTemp;
			nextTemp++;
		}
		else {
			temp = tempRecycle.pop();
		}
		if (mname == null)
			tempIdent.push(null);
		else
			tempIdent.push(mname);
		tempNums.push(temp);
		if (args == null) {
			System.err.println("Pushing data of type null!");
			tempData.push(new StructData("null"));
		}
		else {
//			if (args.equals("int[]"))
//				tempData.push(new StructData());
//			else
//				tempData.push(new StructData(-1)); //TO BE EDITED!!!!!!!!!!!!!!!!!!!!!!!!
			tempData.push(new StructData(args));
		}
		currentFrameTemps++;
//		System.err.println("Temp " + Integer.toString(temp) + " has identifier " + mname + " and type " + args + ".");
		return ("TEMP " + Integer.toString(temp));
	}
	
	String newLabel() {
		nextLabel++;
		//currentFrameLabels++;
		return ("L" + Integer.toString(nextLabel-1));
	}
	
	void emit(String tempb) {
		OutPutBuffer = OutPutBuffer + tempb;
	}
	
	String toTemp(String identifier) {
		int index = tempIdent.indexOf(identifier);
		if (index == -1) {
			System.err.println("Searching for a TEMP linked to an Identifier (" + identifier + ") that does not exist.(@toTemp)");
			return null;
		}
		return ("TEMP " + Integer.toString(tempNums.get(index)));
	}
	
	String toIdentifier(String tempNum) {
		int index = tempNums.indexOf(Integer.parseInt(tempNum.substring(5)));
		if (index == -1) {
			System.err.println("Searching for a IDENTIFIER linked to an TEMP that does not exist.");
			return null;
		}
		return (tempIdent.get(index));
	}
	
	void discard(String tempToBeDiscarded, boolean deletePerm) {
//		System.err.println(tempToBeDiscarded);
		if ( tempToBeDiscarded == null) {
			System.err.println("Tried to discard null instead of TEMP x.");
			return;
		}
		int tmpN = Integer.parseInt(tempToBeDiscarded.substring(5));
		int index = tempNums.indexOf(tmpN);
		if (index == -1)
			return;
		if ((tempIdent.get(index) != null) && !deletePerm)
			return;
		currentFrameTemps--;
		tempRecycle.add(tempNums.remove(index));
		tempIdent.remove(index);
		tempData.remove(index);
		return;
	}
	
	void removeFrame(){ removeFrame(currentFrameTemps); }
	void removeFrame(int n) {
		int temps = -1;
		if (tempNums.size() < n) {
			System.err.println("Trying to remove frame bigger than existing stack.");
		}
		for (int counter = 0; counter < n; counter++) {
			temps = tempNums.peek();
			//emit("Discarding TEMP: " + Integer.toString(temps));
			discard("TEMP " + Integer.toString(temps), true);
		}
	}
	
	String getLength(String identifier) {
		int index = tempIdent.indexOf(identifier);
		if (index == -1) {
			System.err.println("Searching for a TEMP linked to an Identifier (" + identifier + ") that does not exist.(@getLength)");
			return null;
		}
		return tempData.get(index).getArrLeng();
	}
	
	boolean setLength(String identifier, String length) {
		int index = tempIdent.indexOf(identifier);
		if (index == -1) {
			System.err.println("Searching for a TEMP linked to an Identifier (" + identifier + ") that does not exist.(@setLength)");
			return false;
		}
		tempData.get(index).setArrLeng(length);
		return true;
	}
	
	String getVar(String objAddr, String objType, boolean isMethod, String identifier) {
		
		if ((!objType.equals("int")) && (!objType.equals("int[]")) && (!objType.equals("boolean"))) {
			String varType;
			//String fieldVal;
			int offset = 0;
			if (objType.endsWith("*"))
				objType = objType.substring(0, objType.length()-1);
			ClassIn myclass = prevVis.findclassptr(objType);
			if (myclass == null) {
				System.err.println("Cannot find pointer for the " + objType + " class.");
				return null;
			}
//			if (isMethod) {
				if ((offset = searchMethodNameOffset(myclass.methodl, identifier)) == -1) {
					System.err.println("Error while trying to find method offset.");
					return null;
				}
				varType = searchMethodNameType(myclass.methodl, identifier);
				//fieldVal = newTemp(null, varType);
				offset = offset + (myclass.varc * 4);
				//System.err.println(Integer.toString(offset));
//			}
//			else {
//				if ((offset = searchVarNameOffset(myclass.varl, identifier)) == -1) {
//					System.err.println("Error while trying to find var offset.");
//					return null;
//				}
//				varType = searchVarNameType(myclass.varl, identifier);
//				//fieldVal = newTemp(null, varType);
//			}
			String obj = newTemp(null, objType);
			emit("\tHLOAD " + obj + " " + objAddr + " 0\n");
			String varAddr = newTemp(null, varType);
			emit("\tMOVE " + varAddr + " PLUS " + obj + " " + Integer.toString(offset) + "\n");
			return varAddr;
		}
		else
			return null;
	}
	
	public int searchMethodNameOffset(LinkedList<Method> m, String name) {
		ListIterator<Method> li = m.listIterator();
		Method temp;
		int offset = 0;
		while (li.hasNext()) {
			temp = li.next();
			if (temp.name.equals(name))
				return offset;
			offset = offset + 4;
		}
		return -1;
	}
	
	public String searchMethodNameType(LinkedList<Method> m, String name) {
		ListIterator<Method> li = m.listIterator();
		Method temp;
		while (li.hasNext()) {
			temp = li.next();
			if (temp.name.equals(name))
				return temp.type;
		}
		return null;
	}
	
//	public int searchVarNameOffset(LinkedList<Var> m, String name) {
//		ListIterator<Var> li = m.listIterator();
//		Var temp;
//		int offset = 0;
//		while (li.hasNext()) {
//			temp = li.next();
//			if (temp.name.equals(name))
//				return offset;
//			offset = offset + 4;
//		}
//		return -1;
//	}
	
	//ayti kai tin alli na tis kanw implementation me offset/4 ws index?
	public String searchVarNameType(LinkedList<Var> m, String name) {
		ListIterator<Var> li = m.listIterator();
		Var temp;
		while (li.hasNext()) {
			temp = li.next();
			if (temp.name.equals(name))
				return temp.type;
		}
		return null;
	}
	
	public int getClassAllocationSize(String className) {
		ClassIn cls_ptr = prevVis.findclassptr(className);
		if (cls_ptr == null)
			return -1;
		else
			return (cls_ptr.metc * 4)/*VTable*/ + (cls_ptr.varc * 4)/*Vars*/;
	}
	
	public String getThisType() { /*return getTypeFromTemp("TEMP 0");*/ return currentThisType; }
	public String getTypeFromTemp(String tempName) {
		if (tempName == null){
			System.err.println("Trying to get the type of temp that did not initialize correctly.");
			return null;
		}
		int index = tempNums.indexOf(Integer.parseInt(tempName.substring(5)));
		if (index == -1) {
			System.err.println("Searching for a Type linked to a TEMP (" + tempName + ") that does not exist.(@getType)");
			return null;
		}
		return tempData.get(index).sType;
	}
	
	
	public int getSizeOfClass(String className) {
		ClassIn mcl = prevVis.findclassptr(className);
		if (mcl == null)
			return -1;
		else {
			return (mcl.metc + mcl.varc + 1)*4;
		}
	}
	
	
}

