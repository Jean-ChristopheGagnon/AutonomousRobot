package Localization;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
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

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private Navigation navigator;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private double radius;
	private double width;
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double radius, double width) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.navigator = new Navigation(odo, leftMotor, rightMotor, this.radius, this.width);
		//this.navigator = Lab4.navigator;
	}
	
	public void doLocalization() {
		navigator.turnTo(this.leftMotor, this.rightMotor, this.radius, this.width, 315);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		//colorSensor.fetchSample(colorData, 0);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	//	while (colorData[0] < 0.2) {
	//		leftMotor.setSpeed(111);
	//		rightMotor.setSpeed(111);
	//		leftMotor.rotate(convertDistance(this.radius, 11), true);
	//		rightMotor.rotate(convertDistance(this.radius, 11), false);
	//		colorSensor.fetchSample(colorData, 0);
	//	}
			
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

}
