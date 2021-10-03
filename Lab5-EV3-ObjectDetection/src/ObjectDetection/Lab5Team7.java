package ObjectDetection;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

public class Lab5Team7 {

	// Static Resources: motors and sensors
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor clawMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPort = LocalEV3.get().getPort("S2");		

	
	public static void main(String[] args) {
		
		//Setup ultrasonic sensor
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		//Setup color sensor
		@SuppressWarnings("resource")
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorID = colorSensor.getMode("ColorID");		//colorID provides one of the 8 color ID's corresponding integers
		SampleProvider colorValue = colorSensor.getMode("RGB");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
				
		// setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		LCDInfo lcd = new LCDInfo(odo, usValue, usData);
		ObjectDetection detector = new ObjectDetection(usValue, usData, colorValue, colorData, colorID, odo, clawMotor);
		lcd.display("LEFT -- demo_part1", 0, 3);
		lcd.display("RIGHT-- demo_part2", 0, 4);
		
		int button;
		do{
			button = Button.waitForAnyPress();
		}while (button != Button.ID_LEFT && button != Button.ID_RIGHT);//wait for proper putton choice
		
		if(button == Button.ID_LEFT)
		{
			//start detection
			detector.startDetection();
		}
		else
		{
			//start searching
			detector.startSearching();
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	
		
	}

}
