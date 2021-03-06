// Lab3.java
//team 7

package navigation;

import navigation.UltrasonicPoller;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab3 {
	
	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor usMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

	// Constants
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 15.8;

	public static void main(String[] args) {
		int buttonChoice;

		// some objects that need to be instantiated
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		Odometer odometer = new Odometer(leftMotor, rightMotor);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer,t);
		final Navigator navigator = new Navigator(odometer, leftMotor, rightMotor, usMotor, WHEEL_RADIUS, TRACK);
		//OdometryCorrection odometryCorrection = new OdometryCorrection(odometer);
		
		@SuppressWarnings("resource")							    // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);		// usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance");	// usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()];
		
		UltrasonicPoller usPoller = null;
		
		usPoller = new UltrasonicPoller(usDistance, usData, navigator);
		
		do {
			// clear the display
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("  EZ   | Avoid  ", 0, 1);
			t.drawString(" Mode  | Mode   ", 0, 2);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			odometer.start();
			odometryDisplay.start();
			usPoller.start();
			//odometryCorrection.start();
			(new Thread() {
				public void run() {
					double[][] waypoints = new double[][] {new double[] {60,30}, new double[] {30,30}, new double[] {30,60}, new double[] {60,0}};
					navigator.drive(waypoints, false);
				}
			}).start();
		} else if (buttonChoice == Button.ID_RIGHT) {
			odometer.start();
			odometryDisplay.start();
			usPoller.start();
			//odometryCorrection.start();
			(new Thread() {
				public void run() {
					double[][] waypoints = new double[][] {new double[] {0,60}, new double[] {60,0}};
					navigator.drive( waypoints, true);
				}
			}).start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}