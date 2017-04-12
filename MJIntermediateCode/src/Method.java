import java.util.LinkedList;

public class Method {
	public String name;
	public String type;
	public boolean isInherited = false;
	public int varc = 0;
	public LinkedList<Var> varl = new LinkedList<Var>();
	public LinkedList<String> formaltl  = new LinkedList<String>();
	public LinkedList<String> formalnl  = new LinkedList<String>();
	public Method(String n, String t) { name = n; type = t; }
	public Method(String n, String t, boolean ii) { name = n; type = t; isInherited = ii; }
}
