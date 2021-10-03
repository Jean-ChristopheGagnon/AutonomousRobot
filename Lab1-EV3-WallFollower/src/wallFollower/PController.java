//team 7

package wallFollower;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 120, FILTER_OUT = 20;
	private int counter = 0;
	private EV3LargeRegulatedMotor leftMotor, rightMotor, usMotor;
	private int distance;
	private int filterControl;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor usMotor,
					   int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.usMotor = usMotor;
		leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}
	
	@Override
	public void processUSData(int distance) {
		
		// rudimentary filter - toss out invalid samples corresponding to null signal.
		// (n.b. this was not included in the Bang-bang controller, but easily could have).
		//
		if (distance == 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
		} else if (distance == 255){
			// true 255, therefore set distance to 255
			this.distance = distance;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			this.distance = distance;
		}
		
		this.counter = this.counter + 1;
		if( counter == 22){
			usMotor.setSpeed(180);
			usMotor.forward();
		}
		else if( counter == 29){
			usMotor.setSpeed(0);
			usMotor.backward();
		}
		else if( counter == 51){
			usMotor.setSpeed(180);
			usMotor.backward();
		}
		else if( counter == 58){
			usMotor.setSpeed(0);
			usMotor.forward();
			this.counter = 0;
		}
		double factor = this.distance - this.bandCenter;
		if (factor > 10){
			factor = 10;
		}
		if (factor < 0 ){
			factor = 0;
		}
		this.distance = distance;
		if(this.distance <= this.bandCenter - this.bandwidth){
			//int speed = (int)(-this.motorStraight*factor/20);
			leftMotor.setSpeed((int)(this.motorStraight*2));
			leftMotor.forward();
			rightMotor.setSpeed((int)(this.motorStraight*2));
			rightMotor.backward();
		//}else if(this.distance >= this.bandCenter + this.bandwidth){
			//leftMotor.setSpeed((int)((this.motorStraight)*(1-factor/60)));
			//leftMotor.forward();
			//rightMotor.setSpeed(this.motorStraight);
			//rightMotor.forward();
		}else{
			leftMotor.setSpeed((int)((this.motorStraight)*(1-factor/20)));
			leftMotor.forward();
			//leftMotor.setSpeed(this.motorStraight);
			rightMotor.setSpeed(this.motorStraight);
			rightMotor.forward();

		}
	}
			

	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
