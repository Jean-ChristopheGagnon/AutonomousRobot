//team 7

package navigation;

public interface UltrasonicController {
	
	public void processUSData(int distance);
	
	public int readUSDistance();
}
