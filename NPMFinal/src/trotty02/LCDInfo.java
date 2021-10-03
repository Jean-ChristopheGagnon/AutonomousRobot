package trotty02;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * 
 * @author Team 02
 *
 */
public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();;
	private ObjectDetector OD;
	// arrays for displaying data
	private double [] pos;
	private String detection;
	private UltrasonicPoller usPollerF;
	private UltrasonicPoller usPollerS;
	private LightPoller lightPoller;
	/**
	 * Constructor for the LDCInfo class, also starting the LCD timer.
	 * @param odo odometer object
	 * @param usPoller ultrasonic poller object
	 * @param lightPoller light poller object
	 */
	public LCDInfo(Odometer odo, UltrasonicPoller usPollerF, UltrasonicPoller usPollerS, LightPoller lightPoller) {
		this.usPollerF = usPollerF;
		this.usPollerS = usPollerS;

		this.lightPoller = lightPoller;
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
	/**
	 * Displays on screen what the robot perceives
	 */
	public void timedOut() { 
		odo.getPosition(pos);
		
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawInt((int)(pos[0]), 3, 0);
		LCD.drawInt((int)(pos[1]), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
		
		LCD.drawString("DistFront: ", 0, 3);
		LCD.drawString("DistSide: ", 0, 4);
		LCD.drawInt((int) usPollerF.getDistance(), 11, 3);
		LCD.drawInt((int) usPollerS.getDistance(), 10, 4);
		
		LCD.drawString("RBG: ", 0, 5);
		LCD.drawInt((int) Math.floor(this.lightPoller.getRGB()[1]), 5, 5);
		
		
		if(odo.seesSomething()){
		LCD.drawString("Object Detected.", 0, 6);
			if(odo.seesBlock()){
				LCD.drawString("Block!", 0, 7);
			}
			else{
				LCD.drawString("Not Block!", 0, 7);
			}
		}
		else{
			LCD.drawString("No Object", 0, 6);
		}
		
		
	}
	
	/**
	 * displays a string on the screen
	 * @param showString the string to be displayed on the screen
	 */
	public void displayString(String showString) { 
		LCD.clear();
		LCD.drawString(showString, 0, 1);
	}
	
}