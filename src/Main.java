import syntaxtree.*;
import visitor.*;
import java.io.*;

class Main {
    public static void main (String [] args){
		if(args.length < 1){
		    System.err.println("Usage: java Main <inputFile>");
		    System.exit(1);
		}
		FileInputStream fis = null;
		try{
			for (int counter = 0; counter < args.length; counter++) {
			    fis = new FileInputStream(args[counter]);
			    MiniJavaParser parser = new MiniJavaParser(fis);
			    MJVisitor visit = new MJVisitor();
			    Goal root = parser.Goal();
			    System.out.print("\n\n\n\n");
			    System.out.println("File: " + args[counter] + ".");
			    System.err.println("Program parsed successfully.");
			    System.out.println(root.accept(visit, null));
			}
		}
		catch(ParseException ex){
		    System.out.println(ex.getMessage());
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
    }
}