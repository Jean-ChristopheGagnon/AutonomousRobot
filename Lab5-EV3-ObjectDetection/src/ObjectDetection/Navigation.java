package ObjectDetection;

/*
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class Navigation {
	final static int FAST = 200, SLOW = 150, VERYSLOW = 20, ACCELERATION = 4000;
	final static double DEG_ERR = 1.0, CM_ERR = 2;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private boolean obstacle = false;
	private final int FILTER_OUT = 10;
	private SampleProvider usSensor;
	private float[] usData;
	private int filterControl;
	private static final int DETECTION_DISTANCE = 10;

	public Navigation(Odometer odo, SampleProvider usSensor, float[] usData) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		this.usSensor = usSensor;
		this.usData = usData;
		this.filterControl = 0;

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		double minAng;
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
		if (minAng < 0)
				minAng += 360.0;
		this.turnTo(minAng, true);
		this.setSpeeds(FAST, FAST);
		while(isNavigating(x,y));
		this.setSpeeds(0, 0);
	}
	
	private boolean isNavigating(double destX, double destY)
	{
		
		//if destination reached, stop moving
		if(Math.abs(destX - odometer.getX()) > CM_ERR || Math.abs(destY - odometer.getY()) > CM_ERR)
		{
			return true;	
		}
		else
			return false;	//else keep on moving
	}
	
	//use the US sensor to see if an obstacle is detected
	public boolean blocked()
	{
		return getUsData() < DETECTION_DISTANCE;
	}
	
	public boolean blocked(int distance)
	{
		return (getUsData() < distance);
	}
	
	//get filtered us data
	private float getUsData() {
		float distance;
		do{
			usSensor.fetchSample(usData, 0);
			distance = 100 * usData[0];
			
			//filter out spurious 10s
			if(distance <= 10 && filterControl < FILTER_OUT)
			{
				filterControl++;
			}
			else if(distance > 10)
			{
				filterControl = 0;
			}
		}while(filterControl < FILTER_OUT && filterControl > 0);
		
		return distance;
	}
	
	//obstacle getter and setter
	public boolean getObstacle()
	{
		return obstacle;
	}
	public void setObstacle(boolean obs)
	{
		this.obstacle = obs;
	}
	
	//detection distance getter
	public static int getDetectionDistance()
	{
		return Navigation.DETECTION_DISTANCE;
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	/*
	 * Go foward a set distance in cm
	 */
	
	public void goForward(double distance) {
		this.travelTo(this.odometer.getX() + Math.cos(Math.toRadians(this.odometer.getAng())) * distance, this.odometer.getY() + Math.sin(Math.toRadians(this.odometer.getAng())) * distance);

	}
	
	public void stop()
	{
		this.setSpeeds(0, 0);
	}
}
