//Team 7

package wallFollower;
import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private int counter = 0;
	private int distance;
	private EV3LargeRegulatedMotor leftMotor, rightMotor, usMotor;
	
	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor usMotor,
							  int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.usMotor = usMotor;
		leftMotor.setSpeed(motorHigh);				// Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	@Override
	public void processUSData(int distance) {
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
		this.distance = distance;
		if(this.distance <= this.bandCenter - this.bandwidth){
			leftMotor.setSpeed(this.motorHigh*2);
			leftMotor.forward();
			rightMotor.setSpeed(this.motorHigh*2);
			rightMotor.backward();
		}else if(this.distance >= this.bandCenter + this.bandwidth){
			leftMotor.setSpeed((int)(this.motorLow));
			leftMotor.forward();
			rightMotor.setSpeed(this.motorHigh);
			rightMotor.forward();
		}else{
			leftMotor.setSpeed(this.motorHigh);
			leftMotor.forward();
			rightMotor.setSpeed(this.motorHigh);
			rightMotor.forward();
		}
	}
	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
