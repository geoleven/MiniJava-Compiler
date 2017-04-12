import java.util.LinkedList;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;

import syntaxtree.*;
import visitor.GJDepthFirst;
import iris.*;

public class SpigletVisitor extends GJDepthFirst<String, String> {

	boolean instructionTuple = false;
	boolean variableTuple = false;
	boolean nextTuple = false;
	boolean variableMoveTuple = false;
	boolean constantMoveTuple = false;
	boolean variableUseTuple = false;
	boolean variableDefineTuple = false;
	boolean optB = false;
	int deadCounter = 0;
	
	public LinkedList<IRelation> relationsList = new LinkedList<IRelation>();

	String emitString;
	int statementCounter;// = 0;
	boolean mapLabels;
	LinkedList<String> labelName;
	LinkedList<Integer> labelInstructionSequenceNumber;
	String lastLabel = null;

	public static final void pe(String s) {
		System.err.print(s);
	}

	public static final void pen(String s) {
		System.err.println(s);
	}

	public static final void po(String s) {
		System.out.print(s);
	}

	public static final void pon(String s) {
		System.out.println(s);
	}

	public final void emit(String s) {
		emitString += s;
	}

	public String visit(Goal n, String args) {
		emitString = "";

		if (args.equals("instructionTuple"))
			instructionTuple = true;
		else if (args.equals("variableTuple"))
			variableTuple = true;
		else if (args.equals("nextTuple"))
			nextTuple = true;
		else if (args.equals("variableMoveTuple"))
			variableMoveTuple = true;
		else if (args.equals("constantMoveTuple"))
			constantMoveTuple = true;
		else if (args.equals("variableUseTuple"))
			variableUseTuple = true;
		else if (args.equals("variableDefineTuple"))
			variableDefineTuple = true;
		else if (args.equals("optB"))
			optB = true;
		else {
			pen("Goal visitor was given wrong argument, exiting.");
			return null;
		}

		if (instructionTuple) {
			statementCounter = 0;
			n.f1.accept(this, "MAIN");
			n.f3.accept(this, null);

			instructionTuple = false;
		} else if (variableTuple) {
			n.f1.accept(this, "MAIN");
			n.f3.accept(this, null);

			variableTuple = false;
		} else if (nextTuple) {
			n.f1.accept(this, "MAIN");
			n.f3.accept(this, null);

			nextTuple = false;
		} else if (variableMoveTuple) {
			n.f1.accept(this, "MAIN");
			n.f3.accept(this, null);

			variableMoveTuple = false;
		} else if (constantMoveTuple) {
			n.f1.accept(this, "MAIN");
			n.f3.accept(this, null);

			constantMoveTuple = false;
		} else if (variableUseTuple) {
			n.f1.accept(this, "MAIN");
			n.f3.accept(this, null);

			variableUseTuple = false;
		} else if (variableDefineTuple) {
			n.f1.accept(this, "MAIN");
			n.f3.accept(this, null);
			
			variableDefineTuple = false;
		} else if (optB) {
			emit("MAIN\n");
			n.f1.accept(this, "MAIN");
			emit("END\n");
			n.f3.accept(this, null);
			
			optB = false;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}

		return emitString;
	}

	public String visit(StmtList n, String args) {
		statementCounter = 1;
//		n.f0.accept(this, args);
		if (instructionTuple) {
			n.f0.accept(this, args);
		} else if (variableTuple) {
			n.f0.accept(this, args);
		} else if (nextTuple) {
			labelName = new LinkedList<String>();
			labelInstructionSequenceNumber = new LinkedList<Integer>();

			statementCounter = 0;
			mapLabels = true;
			n.f0.accept(this, args);

			
			mapLabels = false;
			statementCounter = 1;
			n.f0.accept(this, args);
			
			labelName = null;
			labelInstructionSequenceNumber = null;
		} else if (variableMoveTuple) {
			n.f0.accept(this, args);
		} else if (constantMoveTuple) {
			n.f0.accept(this, args);
		} else if (variableUseTuple) {
			n.f0.accept(this, args);
		} else if (variableDefineTuple) {
			n.f0.accept(this, args);
		} else if (optB) {
			n.f0.accept(this, args);
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(LblStmt n, String args) {
		if (instructionTuple || optB) {
			if (n.f0.present())
				lastLabel = n.f0.accept(this, args);
			n.f1.accept(this, args);
			lastLabel = null;
		} else if (variableTuple) {
			n.f0.accept(this, args);
			n.f1.accept(this, args);
		} else if (nextTuple) {
			if (mapLabels) {
				// Antistrofa gia na auksanetai to statementCounter
				n.f1.accept(this, args);
				n.f0.accept(this, args);
			} else {
				n.f1.accept(this, args);
			}
		} else if (variableMoveTuple) {
			n.f1.accept(this, args);
		} else if (constantMoveTuple) {
			n.f1.accept(this, args);
		} else if (variableUseTuple) {
			n.f1.accept(this, args);
		} else if (variableDefineTuple) {
			n.f1.accept(this, args);
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(Procedure n, String args) {
		if (variableDefineTuple) {
			for (int counter = 0; counter < Integer.parseInt(n.f2.f0.tokenImage); counter++) {
				emit("varDef(\"" + n.f0.f0.tokenImage + "\", 0, \"TEMP " 
						+ counter + "\").\n");
			}
		} else if (optB) {
			emit(n.f0.f0.tokenImage + " [" + n.f2.f0.tokenImage + "]\n");
		}
		n.f4.accept(this, n.f0.f0.tokenImage);
		return null;
	}

	public String visit(Stmt n, String args) {
		if (instructionTuple) {
			if (lastLabel == null)
				emit("instruction(\"" + args + "\", " + statementCounter + ", \""
						+ n.f0.accept(this, args) + "\").\n");
			else
				emit("instruction(\"" + args + "\", " + statementCounter + ", \""
						+ lastLabel + "\t" + n.f0.accept(this, args) + "\").\n");
			statementCounter++;
		} else if (variableTuple) {
			String temp;
			if ((temp = n.f0.accept(this, args)) != null) {
				emit("var(\"" + args + "\", \"" + temp + "\").\n");
			}
		} else if (nextTuple) {
			if (mapLabels)
				statementCounter++;
			else {
				String destination = n.f0.accept(this, args);
				if (destination == null) {
					emit("next(\"" + args + "\", " + statementCounter + ", "
							+ (statementCounter + 1) + ").\n");
				} else {
					if (n.f0.which == 2) { // Cjump
						emit("next(\"" + args + "\", " + statementCounter
								+ ", " + (statementCounter + 1) + ").\n");
					}
					Integer jumpPoint = labelInstructionSequenceNumber.get(labelName.indexOf(destination));
					emit("next(\"" + args + "\", " + statementCounter + ", "
							+ jumpPoint.toString() + ").\n");
				}
				statementCounter++;
			}
		} else if (variableMoveTuple) {
			if (n.f0.which == 6)
				n.f0.accept(this, args);
			statementCounter++;
		} else if (constantMoveTuple) {
			if (n.f0.which == 6)
				n.f0.accept(this, args);
			statementCounter++;
		} else if (variableUseTuple) {
			n.f0.accept(this, args);
			statementCounter++;
		} else if (variableDefineTuple) {
			n.f0.accept(this, args);
			statementCounter++;
		} else if (optB) {
			//System.err.println(	relationsList.get(3).get(0).get(0).getValue());
			ITuple ftuple = null;
			if (relationsList.get(3).size() > deadCounter) {
				ftuple = relationsList.get(3).get(deadCounter);
				//System.err.println("mplampla");
			}
			if (lastLabel == null) {
				Integer temp = statementCounter;
//				if (ftuple != null)
//					System.err.println(ftuple.get(0).getValue());
				if (ftuple != null && ftuple.get(1).getValue().toString().equals(temp.toString())
						&& ftuple.get(0).getValue().equals(args)) {
					deadCounter++;
				}
				else {
					emit("\t" + n.f0.accept(this, args) + "\n");
				}
			} else {
//				if (ftuple != null)
//					System.err.println(ftuple.get(0).getValue());
				Integer temp = statementCounter;
				if (ftuple != null && ftuple.get(1).getValue().toString().equals(temp.toString())
						&& ftuple.get(0).getValue().equals(args)) {
					deadCounter++;
				}
				emit(lastLabel + "\t" + n.f0.accept(this, args) + "\n");
			}
			statementCounter++;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(NoOpStmt n, String args) {
		if (instructionTuple || optB)
			return n.f0.tokenImage;
		else
			return null;
	}

	public String visit(ErrorStmt n, String args) {
		if (instructionTuple || optB)
			return n.f0.tokenImage;
		else
			return null;
	}

	public String visit(CJumpStmt n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage + " " + n.f1.accept(this, args) + " "
					+ n.f2.accept(this, args);
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return n.f2.f0.tokenImage;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			n.f1.accept(this, args);
		} else if (variableDefineTuple) {
			return null;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(JumpStmt n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage + " " + n.f1.accept(this, args);
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return n.f1.f0.tokenImage;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			return null;
		} else if (variableDefineTuple) {
			return null;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
	}

	public String visit(HStoreStmt n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage + " " + n.f1.accept(this, args) + " "
					+ n.f2.accept(this, args) + " " + n.f3.accept(this, args);
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			n.f1.accept(this, args);
			n.f3.accept(this, args);
		} else if (variableDefineTuple) {
			return null;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(HLoadStmt n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage + " " + n.f1.accept(this, args) + " "
					+ n.f2.accept(this, args) + " " + n.f3.accept(this, args);
		} else if (variableTuple) {
			return n.f1.accept(this, args);
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			//n.f1.accept(this, args);
			n.f2.accept(this, args);
		} else if (variableDefineTuple) {
			n.f1.accept(this, args);
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(MoveStmt n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage + " " + n.f1.accept(this, args) + " "
					+ n.f2.accept(this, args);
		} else if (variableTuple) {
			return n.f1.accept(this, args);
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			if (n.f2.f0.which == 3) {
				String expressionResult = n.f2.accept(this, args);
				if (expressionResult != null) {
					emit("varMove(\"" + args + "\", " + statementCounter + ", \""
							+ n.f1.accept(this, args) + "\", \""
							+ expressionResult + "\").\n");
				}
			}

		} else if (constantMoveTuple) {
			if (n.f2.f0.which == 3) {
				String expressionResult = n.f2.accept(this, args);
				if (expressionResult != null) {
					emit("constMove(\"" + args + "\", " + statementCounter + ", \""
							+ n.f1.accept(this, args) + "\", "
							+ expressionResult + ").\n");
				}
			}

		} else if (variableUseTuple) {
			//n.f1.accept(this, args);
			n.f2.accept(this, args);
		} else if (variableDefineTuple) {
			n.f1.accept(this, args);
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(PrintStmt n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage + " " + n.f1.accept(this, args);
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			n.f1.accept(this, args);
		} else if (variableDefineTuple) {
			return null;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(Exp n, String args) {
		if (instructionTuple || optB) {
			return n.f0.accept(this, args);
		} else if (variableTuple) {
			return n.f0.accept(this, args);
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return n.f0.accept(this, args);
		} else if (constantMoveTuple) {
			return n.f0.accept(this, args);
		} else if (variableUseTuple) {
			n.f0.accept(this, args);
		} else if (variableDefineTuple) {
			n.f0.accept(this, args);
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(StmtExp n, String args) {
		if (instructionTuple) {
			statementCounter = 0;
			n.f1.accept(this, args);
			emit("instruction(\"" + args + "\", " + statementCounter + ", \""
					+ n.f2.tokenImage + " " + n.f3.accept(this, null)
					+ "\").\n");
		} else if (variableTuple) {
			// String temp;
			// if ((temp = n.f1.accept(this, args)) != null) {
			// sgdf
			// }
			n.f1.accept(this, args);
		} else if (nextTuple) {
			n.f1.accept(this, args); 											//args or null?
		} else if (variableMoveTuple) {
			n.f1.accept(this, args);
		} else if (constantMoveTuple) {
			n.f1.accept(this, args);
		} else if (variableUseTuple) {
			n.f1.accept(this, args);
			//statementCounter++;
			n.f3.accept(this, args);
		} else if (variableDefineTuple) {
			n.f1.accept(this, args);
		} else if (optB) {
			emit("BEGIN\n");
			n.f1.accept(this, args);
			emit("RETURN\n\t" + n.f3.accept(this, args) + "\n" + "END\n");
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(Call n, String args) {
		if (instructionTuple || optB) {
			String callArguments = "";
			if (n.f3.present())
				callArguments = n.f3.elementAt(0).accept(this, args);
			for (int counter = 1; counter < n.f3.size(); counter++)
				callArguments += " " + n.f3.elementAt(counter).accept(this, args);
			return n.f0.tokenImage + " " + n.f1.accept(this, args) + " "
					+ n.f2.tokenImage + " " + callArguments + " "
					+ n.f4.tokenImage;
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			n.f1.accept(this, args);
			n.f3.accept(this, args);
		} else if (variableDefineTuple) {
			return null;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(HAllocate n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage + " " + n.f1.accept(this, args);
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			n.f1.accept(this, args);
		} else if (variableDefineTuple) {
			return null;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(BinOp n, String args) {
		if (instructionTuple || optB) {
			return n.f0.accept(this, args) + " " + n.f1.accept(this, args)
					+ " " + n.f2.accept(this, args);
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			n.f1.accept(this, args);
			n.f2.accept(this, args);
		} else if (variableDefineTuple) {
			return null;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(Operator n, String args) {
		if (instructionTuple || optB) {
			return n.f0.choice.toString();
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return null;
		} else if (constantMoveTuple) {
			return null;
		} else if (variableUseTuple) {
			return null;
		} else if (variableDefineTuple) {
			return null;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
	}

	public String visit(SimpleExp n, String args) {
		if (instructionTuple || optB) {
			return n.f0.accept(this, args);
		} else if (variableTuple) {
			return n.f0.accept(this, args);
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			if (n.f0.which == 0)
				return n.f0.accept(this, args);
		} else if (constantMoveTuple) {
			if (n.f0.which == 0)
				return null;
			else
				return n.f0.accept(this, args);
		} else if (variableUseTuple) {
			n.f0.accept(this, args);
		} else if (variableDefineTuple) {
			n.f0.accept(this, args);
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(Temp n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage + " " + n.f1.accept(this, args);
		} else if (variableTuple) {
			return n.f0.tokenImage + " " + n.f1.f0.tokenImage;
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return n.f0.tokenImage + " " + n.f1.f0.tokenImage;
		} else if (constantMoveTuple) {
			return n.f0.tokenImage + " " + n.f1.f0.tokenImage;
		} else if (variableUseTuple) {
			emit("varUse(\"" + args + "\", " + statementCounter + ", \"" 
					+ n.f0.tokenImage + " " + n.f1.f0.tokenImage + "\").\n");
		} else if (variableDefineTuple) {
			emit("varDef(\"" + args + "\", " + statementCounter + ", \"" 
					+ n.f0.tokenImage + " " + n.f1.f0.tokenImage + "\").\n");
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}

	public String visit(IntegerLiteral n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage;
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
			return null;
		} else if (variableMoveTuple) {
			return n.f0.tokenImage;
		} else if (constantMoveTuple) {
			return n.f0.tokenImage;
		} else if (variableUseTuple) {
			return n.f0.tokenImage;
		} else if (variableDefineTuple) {
			return n.f0.tokenImage;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
	}

	public String visit(Label n, String args) {
		if (instructionTuple || optB) {
			return n.f0.tokenImage;
		} else if (variableTuple) {
			return null;
		} else if (nextTuple) {
//			if (mapLabels) {
				labelName.push(n.f0.tokenImage);
				labelInstructionSequenceNumber.push(statementCounter);
//			} else {
////				// ?needed????
//			}
		} else if (variableMoveTuple) {
			return n.f0.tokenImage;
		} else if (constantMoveTuple) {
			return "\"" + n.f0.tokenImage + "\"";
		} else if (variableUseTuple) {
			return n.f0.tokenImage;
		} else if (variableDefineTuple) {
			return n.f0.tokenImage;
		} else {
			pen("Wrong argument given, no such visitor.");
			return null;
		}
		return null;
	}
}
