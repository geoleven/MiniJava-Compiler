public class Var {
	public String type;
	public String name;
	public boolean isInherited = false;
	public Var(String n, String t) { name = n; type = t; }
	public Var(String n, String t, boolean ii) { name = n; type = t; isInherited = ii; }
}
