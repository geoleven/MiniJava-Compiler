import java.util.LinkedList;

public class ClassIn {
	public String name;
	public int varc;
	public int metc;
	public boolean isBase;
	public ClassIn supclass;
	public LinkedList<Var> varl  = new LinkedList<Var>();
	public LinkedList<Method> methodl = new LinkedList<Method>();
	public ClassIn(String n) { name = n; supclass = null; isBase = true; }
	public ClassIn(String n, ClassIn bc) { name = n; supclass = bc; isBase = false; }
}
