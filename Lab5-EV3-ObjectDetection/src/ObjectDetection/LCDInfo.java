package ObjectDetection;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.robotics.SampleProvider;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();
	private SampleProvider usSensor;//--
	private float[] usData;//--
	private final int FILTER_OUT = 20;//--
	private int filterControl;//--
	
	// arrays for displaying data
	private double [] pos;
	
	public LCDInfo(Odometer odo, SampleProvider usSensor, float[] usData) {	//--
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		this.usSensor = usSensor;//--
		this.usData = usData;//--
		this.filterControl = 0;//--
		
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
	
	public void timedOut() { 
		odo.getPosition(pos);
		LCD.clear(0);
		LCD.clear(1);
		LCD.clear(2);
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("U:", 7, 0);//--
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawInt((int)(pos[0] ), 3, 0);
		LCD.drawInt((int)(pos[1] ), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
		LCD.drawString(""+getFilteredData(), 9, 0);//--
	}
	
	public void display(String s,int a, int b)
	{
		this.LCD.drawString(s,a,b);
	}
	
	//--
	private float getFilteredData() {
		float distance;
		do{
			usSensor.fetchSample(usData, 0);
			distance = usData[0] * 100;
			
			//limit the max distance to be 60
			if(distance > 60)
				distance = 60;
			
			//filter out spurious 60s
			if(distance == 60 && filterControl < FILTER_OUT)
			{
				filterControl++;
			}
			else if(distance < 60)
			{
				filterControl = 0;
			}
		}while(filterControl < FILTER_OUT && filterControl > 0);
		
		return distance;
	}
}
