import syntaxtree.*;
import visitor.*;

import java.io.*;

class Main {
    public static void main (String [] args){
    	boolean toStdOut = false;
		if(args.length < 1){
		    System.err.println("Usage: java Main <inputFile>");
		    System.exit(1);
		}
		FileInputStream fis = null;
		try{
			for (int counter = 0; counter < args.length; counter++) {
				if (args[counter].equals("-noFile"))
						toStdOut = true;
				else {
				    fis = new FileInputStream(args[counter]);
				    MiniJavaParser parser = new MiniJavaParser(fis);
				    MJVisitor typeCheck = new MJVisitor();
				    MJSPGVisitor visit = new MJSPGVisitor();
				    Goal root = parser.Goal();
//				    System.err.print("\n\n\n\n");
//				    System.err.println("File: " + args[counter] + ".");
//				    System.err.println("Program parsed successfully.");
				    String typeErrors = root.accept(typeCheck, null);
				    System.err.print(typeErrors);
				    visit.prevVis = typeCheck;
				    
				    
				    System.err.println("Class types declared:");
				    //System.err.println(typeCheck.declaredClassList.toString());
				    //System.err.println("Class instances declared:");
				    int[] c = {1};
				    typeCheck.classl.forEach((cls) -> { 
				    	System.err.println(c[0] + ": " + cls.name); 
				    	c[0]++; 
				    	System.err.println("\tMethods:");
				    	int [] c1 = {1};
				    	cls.methodl.forEach(msl -> { 
				    		System.err.println("\t\t" + c1[0] + ": " + msl.name + " -> " + msl.type);
				    		c1[0]++;
				    	});
				    	System.err.println("\tVars:");
				    	c1[0] = 1;
				    	cls.varl.forEach(vsl -> { 
				    		System.err.println("\t\t" + c1[0] + ": " + vsl.name + " -> " + vsl.type);
				    		c1[0]++;
				    	});
				    });
//				    c[0] = 1;
//				    System.err.println(typeCheck.allReachableVarsList.size());
//				    typeCheck.allReachableVarsList.forEach((mvar) -> { System.err.println(c[0]+ ": " + mvar.type + " -> " + mvar.name ); c[0]++; });
				    
				    System.err.println("Procceding to generate intermediate code.");
				    if (typeErrors.equals("Type check: ok.\n")){
						if (toStdOut)
							System.out.println(root.accept(visit, null));
						else {
							Writer writer = null;
							try {
								writer = new BufferedWriter(new OutputStreamWriter(
										new FileOutputStream(args[counter].substring(0, args[counter].lastIndexOf(".java")) + ".spg"),
										"utf-8"));
								writer.write(root.accept(visit, null));
							} catch (IOException ex) {
							} finally {
								try {
									writer.close();
								} catch (Exception ex) {
								}
							}
						}
				    }
				    else {
				    	System.err.println("Exiting now");
				    }
				}
			}
		}
		catch(ParseException ex){
		    System.err.println(ex.getMessage());
		}
		catch(FileNotFoundException ex){
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