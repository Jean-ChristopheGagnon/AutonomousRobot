package random;

public class randyClass {
	int doStuff(int A){
		int B = 1;
		if (A < 2 ){
			return A ; 
		}
		int C = 1;
		for (int i=2;i < A; i++ ) {
			int D = B;
			B += C;
			C = D;
		}
		return B;
	}
	
	public static void main(String args[]){
		randyClass randy = new randyClass();
		for (int i=0;i<22;i++){
			System.out.println(randy.doStuff(i));
		}
		
	}
}
