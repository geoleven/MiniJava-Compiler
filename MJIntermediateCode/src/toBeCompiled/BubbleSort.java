class Test{
	public static void main(String[] a){
		int [] a;
		int b;
		int c;
		int d;

		Mple ampla;

		ampla = new Mple();

		c = ampla.Start(ampla.Start(4));
		System.out.println(c);		



	}
}


class Mple {	
	int a;
	int b;
	public int Start(int d) {
		int a;
		System.out.println(1);
		a = d + (this.dyo());
		return a; 
	}
	public int dyo() {
		System.out.println(2);
		return 55;
	}
	public int tr() {
		return 4;
	}
}
