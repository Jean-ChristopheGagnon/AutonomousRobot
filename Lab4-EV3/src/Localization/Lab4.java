package Localization;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import Localization.UltrasonicPoller;
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
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class Lab4{

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPort = LocalEV3.get().getPort("S2");		
	public static final double radius = 2.1;
	public static final double width = 15.8;

	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		int buttonChoice;
		int buttonChoiceTwo;
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor}) {
			motor.stop();
			motor.setAcceleration(1111);
		}
		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		UltrasonicPoller usPoller = null;
		Odometer odo = new Odometer(leftMotor, rightMotor, radius, width);
		USLocalizer usl = new USLocalizer(odo, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE, leftMotor, rightMotor, radius, width);
		
		usPoller = new UltrasonicPoller(usValue, usData, usl);
		
		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("Red");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
				
		// setup the odometer and display

		LCDInfo lcd = new LCDInfo(odo);
		do {
			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_ENTER);
		// perform the ultrasonic localization
		odo.start();
		usPoller.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		usl.doLocalization();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		do {
			buttonChoiceTwo = Button.waitForAnyPress();
		} while (buttonChoiceTwo != Button.ID_ENTER);
		LightLocalizer lsl = new LightLocalizer(odo, colorValue, colorData, leftMotor, rightMotor, radius, width);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		lsl.doLocalization();			
		
		while (Button.waitForAnyPress() != Button.ID_ENTER);
		System.exit(0);
		
	}
	

}
