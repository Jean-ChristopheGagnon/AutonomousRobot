/*
 * Odometer.java
 */

package Localization;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Timer;
import lejos.utility.TimerListener;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;
	
	public static int lastTachoL;			
	public static int lastTachoR;			 
	public static int nowTachoL;			
	public static int nowTachoR;			
	public static double X;					
	public static double Y;					
	public static double Theta;
	double distL, distR, deltaD, deltaT, dX, dY;		// Wheelbase (cm)
	public static double WR;
	public static double WB;			// Wheel radius (cm)

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor, double radius, double width) {
		this.WR = radius;
		this.WB = width;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();
			// This block of code comes from the odoDemo code provided on MyCourses

						nowTachoL = leftMotor.getTachoCount();      		// get tacho counts
						nowTachoR = rightMotor.getTachoCount();
						distL = Math.PI*WR*(nowTachoL-lastTachoL)/180;		// compute L and R wheel displacements
						distR = Math.PI*WR*(nowTachoR-lastTachoR)/180;
						lastTachoL=nowTachoL;								// save tacho counts for next iteration
						lastTachoR=nowTachoR;
						deltaD = 0.5*(distL+distR);							// compute vehicle displacement
						deltaT = (distL-distR)/WB;							// compute change in heading
						Theta += deltaT;										// update heading
						if (Theta < 0){
							Theta += 2*Math.PI; 
						}
						if (Theta > (2*Math.PI)){
							Theta = 0;
						}
						this.dX = deltaD * Math.sin(Theta);						// compute X component of displacement
						this.dY = deltaD * Math.cos(Theta);						// compute Y component of displacement
						X = X + dX;											// update estimates of X and Y position
						Y = Y + dY;

			synchronized (lock) {
				this.x = X;
				this.y = Y;
				this.theta = Theta/(2*(Math.PI))*360;
				//theta = 0.7854; //TODO replace example value
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
}