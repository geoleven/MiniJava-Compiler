import syntaxtree.*;
import visitor.*;
import iris.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

	public static void main(String[] args) throws Exception {
		if(args.length < 1){
		    System.err.println("Usage: java Main <inputFile>");
		    System.exit(1);
		}
		FileInputStream fis = null;
		try{
			for (int counter = 0; counter < args.length; counter++) {
				    fis = new FileInputStream(args[counter]);
				    SpigletParser parser = new SpigletParser(fis);
				    SpigletVisitor visit = new SpigletVisitor();
				    Goal root = parser.Goal();
//				    System.err.print("\n\n\n\n");
//				    System.err.println("File: " + args[counter] + ".");
//				    System.err.println("Program parsed successfully.");
//				    String instructionTuple = root.accept(visit, "instructionTuple");
//				    String variableTuple = root.accept(visit, "variableTuple");
//				    String nextTuple = root.accept(visit, "nextTuple");
//				    String variableMoveTuple = root.accept(visit, "variableMoveTuple");
//				    String constantMoveTuple = root.accept(visit, "constantMoveTuple");
//				    String variableUseTuple = root.accept(visit, "variableUseTuple");
//				    String variableDefineTuple = root.accept(visit, "variableDefineTuple");
				    
					Writer writer = null;
					try {
						if (Files.notExists(Paths.get("./analyses"))) {
							new File("./analyses").mkdir();
						}
						if (Files.notExists(Paths.get("./analyses/facts"))) {
							new File("./analyses/facts").mkdir();
						}
						if (Files.notExists(Paths.get("./optOut"))) {
							new File("./optOut").mkdir();
						}
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream("analyses/facts/" + args[counter].substring(0, args[counter].lastIndexOf(".spg")).substring(args[counter].lastIndexOf("/")+1, args[counter].length()-4) + ".instruction.iris"),
								"utf-8"));
						writer.write(root.accept(visit, "instructionTuple"));
						//System.err.println(root.accept(visit, "instructionTuple"));
						writer.close();
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream("./analyses/facts/" + args[counter].substring(0, args[counter].lastIndexOf(".spg")).substring(args[counter].lastIndexOf("/")+1, args[counter].length()-4) + ".var.iris"),
								"utf-8"));
						writer.write(root.accept(visit, "variableTuple"));
						writer.close();
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream("./analyses/facts/" + args[counter].substring(0, args[counter].lastIndexOf(".spg")).substring(args[counter].lastIndexOf("/")+1, args[counter].length()-4) + ".next.iris"),
								"utf-8"));
						writer.write(root.accept(visit, "nextTuple"));
						writer.close();
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream("./analyses/facts/" + args[counter].substring(0, args[counter].lastIndexOf(".spg")).substring(args[counter].lastIndexOf("/")+1, args[counter].length()-4) + ".varMove.iris"),
								"utf-8"));
						writer.write(root.accept(visit, "variableMoveTuple"));
						writer.close();
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream("./analyses/facts/" + args[counter].substring(0, args[counter].lastIndexOf(".spg")).substring(args[counter].lastIndexOf("/")+1, args[counter].length()-4) + ".constMove.iris"),
								"utf-8"));
						writer.write(root.accept(visit, "constantMoveTuple"));
						writer.close();
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream("./analyses/facts/" + args[counter].substring(0, args[counter].lastIndexOf(".spg")).substring(args[counter].lastIndexOf("/")+1, args[counter].length()-4) + ".varUse.iris"),
								"utf-8"));
						writer.write(root.accept(visit, "variableUseTuple"));
						writer.close();
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream("./analyses/facts/" + args[counter].substring(0, args[counter].lastIndexOf(".spg")).substring(args[counter].lastIndexOf("/")+1, args[counter].length()-4) + ".varDef.iris"),
								"utf-8"));
						writer.write(root.accept(visit, "variableDefineTuple"));
						writer.close();
						
						Optimize opt = new Optimize();
						opt.createAnalyses(null);
						//opt.printAnalyses(1);
						visit.relationsList = opt.relationsList;
//						System.err.println(opt.relationsList.get(3).get(0).get(0).getValue());
//						System.err.println(opt.relationsList.get(3).get(0).get(1).getValue());
//						System.err.println(opt.relationsList.get(3).get(0).get(2).getValue());
						writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream("./optOut/" + args[counter].substring(0, args[counter].lastIndexOf(".spg")).substring(args[counter].lastIndexOf("/")+1, args[counter].length()-4) + ".opt.spg"),
								"utf-8"));
						writer.write(root.accept(visit, "optB"));
						writer.close();	
						
						
					} catch (IOException ex) {
						System.err.println("IO error.");
						ex.printStackTrace();
					}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(ParseException ex){
		    System.err.println(ex.getMessage());
		}
		finally{
		    try{
				if(fis != null) fis.close();
		    }
		    catch(IOException ex){
				System.err.println(ex.getMessage());
		    }
		}
		System.err.println("Finished. Now exiting.");
	}
}
