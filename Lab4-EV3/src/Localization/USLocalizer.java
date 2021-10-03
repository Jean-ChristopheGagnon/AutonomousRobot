package Localization;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import Localization.UltrasonicController;
import Localization.Odometer;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.lcd.TextLCD;

public class USLocalizer implements UltrasonicController{
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 30;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Navigation navigator;
	private double radius;
	private double width;
	final private double rotationIncrement = 15.0;
	private int distanceOne;
	private int distanceTwo;
	final private int wallDistance = 45;
	private boolean waitForA = false;
	private boolean waitForB = false;
	private double angleA;
	private double angleB;
	private static final int FORWARD_SPEED = 150;
	private static final int ROTATE_SPEED = 150;
	
	
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double radius, double width) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.radius = radius;
		this.width = width;
		this.navigator = new Navigation(odo, leftMotor, rightMotor, this.radius, this.width);
	}
	
	public void doLocalization() {
		double [] pos = new double [3];
		if (locType == LocalizationType.FALLING_EDGE) {
			while (readUSDistance() < wallDistance){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				turnByIncrementClockwise();
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			turnByIncrementClockwise();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			turnByIncrementClockwise();
			this.waitForA = true;
	///		try {
	//			Thread.sleep(500);
	//		} catch (InterruptedException e) {
	//		}
	//		turnByIncrementClockwise();
	//		try {
	//			Thread.sleep(500);
	//		} catch (InterruptedException e) {
	//		}
	//		turnByIncrementClockwise();
			
			// rotate the robot until it sees no wall
			while (this.waitForA){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				turnByIncrementClockwise();
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			turnByIncrementCounterClockwise();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			turnByIncrementCounterClockwise();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			this.waitForB = true;
	//		turnByIncrementCounterClockwise();
	//		try {
	//			Thread.sleep(500);
	//		} catch (InterruptedException e) {
	//		}
	//		turnByIncrementCounterClockwise();
			
			// keep rotating until the robot sees a wall, then latch the angle
			while (this.waitForB){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				turnByIncrementCounterClockwise();
			}
			int corr;
			if (this.angleA > 180){
				corr = 45;
			} else {
				corr = -135;
			}
			double trueZero = ((this.angleA+this.angleB)/2)+corr;
			if (trueZero < 0) {
				trueZero += 360;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			navigator.turnTo(this.leftMotor, this.rightMotor, this.radius, this.width, trueZero);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
			navigator.turnTo(this.leftMotor, this.rightMotor, this.radius, this.width, trueZero-45);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
					leftMotor.setSpeed(111);
					rightMotor.setSpeed(111);
					leftMotor.rotate(convertDistance(this.radius, 11), true);
					rightMotor.rotate(convertDistance(this.radius, 11), false);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
					navigator.turnTo(this.leftMotor, this.rightMotor, this.radius, this.width, 0);
			// switch direction and wait until it sees no wall
			
			// keep rotating until the robot sees a wall, then latch the angle
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			// update the odometer position (example to follow:)
			//odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
		} else {
			while (readUSDistance() > wallDistance){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				turnByIncrementClockwise();
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			turnByIncrementClockwise();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			turnByIncrementClockwise();
			this.waitForA = true;
			// rotate the robot until it sees no wall
			while (this.waitForA){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				turnByIncrementClockwise();
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			turnByIncrementCounterClockwise();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			turnByIncrementCounterClockwise();
			this.waitForB = true;
			// keep rotating until the robot sees a wall, then latch the angle
			while (this.waitForB){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				turnByIncrementCounterClockwise();
			}
			
			double trueZero = ((this.angleA+this.angleB)/2)+45;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			navigator.turnTo(this.leftMotor, this.rightMotor, this.radius, this.width, trueZero);
		}
	}
	
	public void turnByIncrementClockwise () {
		double nextTheta = odo.getTheta() + this.rotationIncrement;
		if (nextTheta > 360){
			nextTheta = nextTheta - 360;
		}
		navigator.turnTo(this.leftMotor, this.rightMotor, this.radius, this.width, nextTheta);
	}
	
	public void turnByIncrementCounterClockwise () {
		double nextTheta = odo.getTheta() - this.rotationIncrement;
		if (nextTheta > 360){
			nextTheta = nextTheta - 360;
		}
		navigator.turnTo(this.leftMotor, this.rightMotor, this.radius, this.width, nextTheta);
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
				
		return distance;
	}
	
	@Override
	public void processUSData(int distance){
		if (distance != 255) {
			this.distanceTwo = this.distanceOne;
			this.distanceOne = distance;
			if (locType == LocalizationType.FALLING_EDGE) {
				if (waitForA && readUSDistance() <= wallDistance){
					this.angleA = odo.getTheta();
					waitForA = false;
				} 
				if (waitForB && readUSDistance() <= wallDistance) {
					this.angleB = odo.getTheta();
					waitForB = false;
				}
		//	} else if (locType == LocalizationType.RISING_EDGE) {
		//		if (waitForA && readUSDistance() >= wallDistance){
		//			this.angleA = odo.getTheta();
		//			waitForA = false;
			//	} else if (waitForB && readUSDistance() >= wallDistance) {
		//			this.angleB = odo.getTheta();
		//			waitForB = false;
		//		}
			}
		}
	}
	
	@Override
	public int readUSDistance() {
		return Math.min(this.distanceOne, this.distanceTwo);
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	

}
