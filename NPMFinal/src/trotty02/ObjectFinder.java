package trotty02;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Delay;
/**
 * 
 * @author Jean-Christophe
 *
 */
public class ObjectFinder {
	double TILE_LENGTH = 30.48;
	double error = 8;
	int MAX_AVOIDANCE_COUNTER = 666;
	int AVOID_SPEED = 100;
	int BAND_CENTER = 12;
	int BAND_WIDTH = 3;
	double seekObjectDistance = TILE_LENGTH;
	double captureDistance = 11;
	boolean avoidMode = false;
	int resolution;
	int i = 0;
	int j = 0;
	double[] depositCoords;
	double[] endCoords; 
	boolean captured = false;
	boolean atDestination = false;

	double[][] waypoints = new double[][]{ //{x, y, check top? 0=true 1=false}
			  {(0+i)*TILE_LENGTH, (1+j)*TILE_LENGTH, 0},
			  {(0+i)*TILE_LENGTH, (2+j)*TILE_LENGTH, 0},
			  {(0+i)*TILE_LENGTH, (3+j)*TILE_LENGTH, 0},
			  {(0+i)*TILE_LENGTH, (4+j)*TILE_LENGTH, 1},
			  {(1+i)*TILE_LENGTH, (4+j)*TILE_LENGTH, 0},
			  {(2+i)*TILE_LENGTH, (4+j)*TILE_LENGTH, 0},
			  {(3+i)*TILE_LENGTH, (4+j)*TILE_LENGTH, 0},
			  {(4+i)*TILE_LENGTH, (4+j)*TILE_LENGTH, 1},
			  {(4+i)*TILE_LENGTH, (3+j)*TILE_LENGTH, 0},
			  {(4+i)*TILE_LENGTH, (2+j)*TILE_LENGTH, 0},
			  {(4+i)*TILE_LENGTH, (1+j)*TILE_LENGTH, 0},
			  {(4+i)*TILE_LENGTH, (0+j)*TILE_LENGTH, 1},
			  {(3+i)*TILE_LENGTH, (0+j)*TILE_LENGTH, 0},
			  {(2+i)*TILE_LENGTH, (0+j)*TILE_LENGTH, 0},
			  {(1+i)*TILE_LENGTH, (0+j)*TILE_LENGTH, 0},		
			  {(1+i)*TILE_LENGTH, (1+j)*TILE_LENGTH, 0},
			  {(1+i)*TILE_LENGTH, (2+j)*TILE_LENGTH, 0},
			  {(1+i)*TILE_LENGTH, (3+j)*TILE_LENGTH, 0},
			  {(2+i)*TILE_LENGTH, (3+j)*TILE_LENGTH, 0},
			  {(3+i)*TILE_LENGTH, (3+j)*TILE_LENGTH, 0},
			  {(3+i)*TILE_LENGTH, (2+j)*TILE_LENGTH, 0},
			  {(3+i)*TILE_LENGTH, (1+j)*TILE_LENGTH, 0},
			  {(2+i)*TILE_LENGTH, (1+j)*TILE_LENGTH, 0},
			  {(2+i)*TILE_LENGTH, (2+j)*TILE_LENGTH, 0},

			}; 
	double[] startPoint = new double []{(0+i)*TILE_LENGTH, (0+j)*TILE_LENGTH, 0};
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Navigation navigator;
	private LightLocalizer lightLocalizer;
	private UltrasonicPoller USpollerF;
	private UltrasonicPoller USpollerS;
	private USLocalizer usLocalizer;
	private double radius;
	private double width;
	private Capture capture;
	private LightPoller lightPoller;

	/**
	 * Searching the board for an object
	 * @param leftMotor	the robot's left wheel motor
	 * @param rightMotor the robot's right wheel motor
	 * @param navigator navigator object
	 * @param odometer odometer object
	 * @param USpoller ultrasonic sensor poller object
	 * @param usLocalizer ultrasonic sensor localizer
	 * @param radius radius of the wheels
	 * @param width track length, distance between the two wheels
	 */
	public ObjectFinder(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Navigation navigator, 
			Odometer odometer,
			UltrasonicPoller USpollerF, UltrasonicPoller USpollerS,
			USLocalizer usLocalizer, LightPoller lightPoller, int resolution
			, int xHalf, int yHalf, Capture capture) {

		this.i = xHalf;
		this.j = yHalf;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.navigator = navigator;

		this.capture = capture;
		this.odometer = odometer;
		this.USpollerF = USpollerF;
		this.USpollerS = USpollerS;
		this.usLocalizer = usLocalizer;
		this.radius = radius;
		this.width = width;
		this.lightPoller = lightPoller;
		this.resolution = resolution;
		this.navigator.setResolution(this.resolution);
	}
	/**
	 * Drives the robot in a square
	 */

	
	public void pointDriver(){
		int i;
		int j;
		int k;
		double sideAngle;
		this.atDestination = false;
		while (this.atDestination == false) {
			goToPoint(startPoint[0], startPoint[1]);
		}
		while (captured == false) {
			for (i = 0; i < waypoints.length; i++) {
				if (!avoidMode) {
					for (j = 0; j < resolution; j++) {
						navigator.travelTo(waypoints[i][0], waypoints[i][1], true);
						try { Thread.sleep(20);  } catch(Exception e){}	
						double frontDist = USpollerF.getDistance();
						double sideDist = USpollerS.getDistance();
						if (frontDist < seekObjectDistance && waypoints[i][2] == 0) {
							if (checkObject(frontDist, 0)){
								captured = true;
								i = 1000;
								j = 1000;
								Delay.msDelay(4000);
							} else {
								sideAngle = odometer.getTheta() + 90;
								while (sideAngle >= 360) {
									sideAngle -= 360;
								}
								navigator.turnTo(sideAngle, true);	
								avoidMode = true;
								j = resolution; //prevents skipping tiles
							}
						} else if (sideDist < seekObjectDistance) {
							if (checkObject(sideDist, 1)){
								captured = true;
								i = 1000;
								j = 1000;
								Delay.msDelay(4000);
							}
						}
					}
				} else {
					for (k = 0; k < MAX_AVOIDANCE_COUNTER; k++){
						bangbang(USpollerS.getDistance());
					}
					i--;
					avoidMode = false;
				}
			}
		}
		this.atDestination = false;
		while (this.atDestination == false) {
			goToPoint(depositCoords[0], depositCoords[1]);
		}
		Delay.msDelay(2500);
		capture.freeObj();
		this.atDestination = false;
		while (this.atDestination == false) {
			goToPoint(endCoords[0], endCoords[1]);
		}
	}
	
	boolean checkObject(double objectDistance, int sensor) { //sensor 0=front, 1=side
		double travelDistance;
		double sideAngle;
		if (objectDistance > captureDistance ) {
			travelDistance = objectDistance - captureDistance;
		} else {
			travelDistance = 0;
		}
		if (sensor == 1) {
			sideAngle = odometer.getTheta() - 90;
			while (sideAngle < 0) {
				sideAngle += 360;
			}
			navigator.turnTo(sideAngle, true);	
		}
		if (travelDistance > 0) {
			try { Thread.sleep(20);  } catch(Exception e){}	
			navigator.goForward(travelDistance);
	
		}
		try { Thread.sleep(100);  } catch(Exception e){}	
		if (lightPoller.isFoamBlock()) {
			double oppAngle = odometer.getTheta() - 180;
			while (oppAngle < 0) {
				oppAngle += 360;
			}
			try { Thread.sleep(20);  } catch(Exception e){}
			navigator.turnTo(oppAngle, true);
			try { Thread.sleep(20);  } catch(Exception e){}
			capture.CaptureObj();
			Delay.msDelay(2500);
			return true;
		} else {
			return false;
		}
	}
	
	void bangbang (double distance) {
		try { Thread.sleep(20);  } catch(Exception e){}
		if(distance >= BAND_CENTER+3 ) {//if it's a little too far to the right, turn in left.
			rightMotor.setSpeed(AVOID_SPEED);
			leftMotor.setSpeed(AVOID_SPEED*2); //speeds up the right motor to turn left
			
			leftMotor.forward(); //apply the changes to the speeds
			rightMotor.forward();
		} else { // if its +/- 3 from the bandCenter, just set the robot straight instead of using a p controller speed
			rightMotor.setSpeed(AVOID_SPEED); //applies normal straight speed
			leftMotor.setSpeed(AVOID_SPEED);
			leftMotor.forward(); //apply the changes to the speeds
			rightMotor.forward();
		}
	}
	
	
	void goToPoint(double xCoord, double yCoord){
		double sideAngle;
		while (getDistance(xCoord, yCoord) > this.error) {
			navigator.travelTo(endCoords[0],endCoords[1], true);
			int i;
			if (USpollerF.getDistance() < 12) {
				sideAngle = odometer.getTheta() - 90;
				while (sideAngle < 0) {
					sideAngle += 360;
				}
				navigator.turnTo(sideAngle, true);	
				for (i = 0; i < MAX_AVOIDANCE_COUNTER; i++) {
					bangbang(USpollerF.getDistance());
				}
			}
		}
		this.atDestination = true;
	}
	
	double getDistance(double xCoord, double yCoord){
		double dx = xCoord - odometer.getX();
		double dy = yCoord - odometer.getY();
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	void setEndCoords(){
		this.endCoords[0] = odometer.getX();
		this.endCoords[1] = odometer.getY();
	}
	
	void setDepositCoords(double x, double y){
		this.depositCoords[0] = x;
		this.depositCoords[1] = y;
	}
}
