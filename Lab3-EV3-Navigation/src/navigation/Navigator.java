/*
//team 7
 */
package navigation;

import navigation.UltrasonicController;
import navigation.Odometer;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.lcd.TextLCD;

public class Navigator extends Thread implements UltrasonicController{
	private Odometer odometer;
	private static final int FORWARD_SPEED = 150;
	private static final int ROTATE_SPEED = 150;
	private static final int bandCenter = 18;
	private static final int bandWidth = 3;
	private boolean isTravelling = false;
	private boolean isTurning = false;
	private boolean avoidMode;
	private EV3LargeRegulatedMotor leftMotor, rightMotor, usMotor;
	private double radius;
	private double width;
	private int distance = 255;
	private boolean objDetected = false;
	private int avoidCounter = 0;
	private int avoidMaxCounter = 1111;
	private int binaryCounter = 0;
	
	public Navigator(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor usMotor, double radius, double width) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.usMotor = usMotor;
		this.radius = radius;
		this.width = width;
	}

	public void drive(double[][] waypoints, boolean avoidMode) {
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor, usMotor }) {
			motor.stop();
			motor.setAcceleration(111);
		}
		this.avoidMode = avoidMode;
		// wait 5 seconds
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}
		for (int i = 0; i < waypoints.length; i++){
			while (!isAtWaypoint(waypoints[i][0], waypoints[i][1])){
				if (!isNavigating() && !objDetected){
					travelTo(leftMotor, rightMotor,radius, width, waypoints[i][0], waypoints[i][1]);
					//if (this.avoidMode == true) {
				//		lookForObject();
				//	}
				}
			//	try {
					//Thread.sleep(2000);
			//	} catch (InterruptedException e) {

				//}
				if (this.avoidMode == true) {
					lookForObject();
				}
			}
		}

		//travelTo(leftMotor, rightMotor,radius, width, 90, 45);
	}
	
	public void travelTo(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double radius, double width, double destX, double destY){
		this.isTravelling = true;
		double dx = destX - odometer.getX();
		double dy = destY - odometer.getY();
		double goalt = 0;
		if(dx>=0 && dy>=0){
			goalt = Math.atan(Math.abs(dx/dy));
		} else if (dx<0 && dy>0) {
			goalt = 2*Math.PI - Math.atan(Math.abs(dx/dy));
		} else if (dx<0 && dy<0) {
			goalt = Math.PI + Math.atan(Math.abs(dx/dy));
		} else{
			goalt = Math.PI - Math.atan(Math.abs(dx/dy));
		}
		double goalDegrees = 180*goalt/Math.PI;

		turnTo(leftMotor, rightMotor, radius, width, goalDegrees);
		double distance = Math.sqrt((dx*dx)+(dy*dy));
		if (distance > 10 && avoidMode){
			distance = 10;
		}
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {

		}
		if (this.binaryCounter % 2 == 0){
			leftMotor.rotate(convertDistance(radius, distance), true);
			rightMotor.rotate(convertDistance(radius, distance), false);
		} else {
			rightMotor.rotate(convertDistance(radius, distance), true);
			leftMotor.rotate(convertDistance(radius, distance), false);	
		}
		this.binaryCounter++;
		this.isTravelling = false;
	}
	
	public void turnTo(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double radius, double width, double theta){
		this.isTurning = true;
		theta = theta - odometer.getTheta();
		while (theta < 0){
			theta = theta + 360;
		}
		while (theta > 360){
			theta = theta - 360;
		}
		if (theta <= 180){
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			leftMotor.rotate(convertAngle(radius, width, theta), true);
			rightMotor.rotate(-convertAngle(radius, width, theta), false);
		} else {
			theta = 360 - theta;
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			leftMotor.rotate(-convertAngle(radius, width, theta), true);
			rightMotor.rotate(convertAngle(radius, width, theta), false);
		}
		this.isTurning = false;
	}
	
	public boolean isAtWaypoint(double destX, double destY){
		return (Math.abs(destX - odometer.getX()) <= 1 && Math.abs(destY - odometer.getY()) <= 1);
	}
	
	public boolean isNavigating(){
		return (this.isTurning || this.isTravelling);
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	@Override
	public void processUSData(int distance){
		this.distance = distance;
		
	}
	
	public void lookForObject(){
		if (!isNavigating()){
			if (!(this.objDetected)){
				if (distance <= bandCenter){
					this.objDetected = true;
					this.avoidCounter = 0;
					usMotor.rotate(-90, true);
					double newTheta = odometer.getTheta() + 90.0;
					if (newTheta > 360){
						newTheta = newTheta - 360;
					}
					turnTo(this.leftMotor, this.rightMotor, this.radius, this.width, newTheta);
				}
			}
			if (this.objDetected){
				if(this.distance <= bandCenter - bandWidth){
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {

					}
					leftMotor.setSpeed((FORWARD_SPEED));
					leftMotor.forward();
					rightMotor.setSpeed((int)(FORWARD_SPEED*0.55));
					rightMotor.forward();
				}else if(this.distance >= bandCenter + bandWidth){
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {

					}
					leftMotor.setSpeed((int)(FORWARD_SPEED*0.55));
					leftMotor.forward();
					rightMotor.setSpeed(FORWARD_SPEED);
					rightMotor.forward();
				}else{
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {

					}
					leftMotor.setSpeed(FORWARD_SPEED);
					leftMotor.forward();
					rightMotor.setSpeed(FORWARD_SPEED);
					rightMotor.forward();
				}
				this.avoidCounter++;
				if (this.avoidCounter > this.avoidMaxCounter){
					usMotor.rotate(90, true);
					this.objDetected = false;
					this.avoidCounter = 0;
				}
			}
		}
	}
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}
}