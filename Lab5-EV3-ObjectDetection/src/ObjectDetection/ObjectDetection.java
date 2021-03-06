package ObjectDetection;

/*The searching mechanism:
 *	First search from 0,0 along positive y axis
 *	Then, search through each row with different y
 *		number of rows depends on the numTravel value
 *
 *	If wooden block is on the y axis
 *  Then search columns instead of rows
 */

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class ObjectDetection{
	
	private boolean block = false;		//true if the object is the Styrofoam block, false otherwise
	private EV3LargeRegulatedMotor clawMotor;
	private SampleProvider usSensor;
	private float[] usData;
	private final int FILTER_OUT = 10;
	private int filterControl;
	private SampleProvider colorValue;
	private float[] colorData;
	private SampleProvider colorID;
	private TextLCD LCD = LocalEV3.get().getTextLCD();
	private Odometer odo;
	private Navigation navi;
	private USLocalizer loc;
	private int numTravel = 3;		//determines the number of rows need to be searched

	private double maxRange;		
	private double tileLength = 30.48;
	
	public ObjectDetection(SampleProvider usSensor, float[] usData, SampleProvider colorValue, float[] colorData, SampleProvider colorID, Odometer odo, EV3LargeRegulatedMotor clawMotor)
	{
		this.usSensor = usSensor;
		this.usData = usData;
		this.filterControl = 0;
		this.colorValue = colorValue;
		this.colorData = colorData;
		this.colorID = colorID;
		this.clawMotor = clawMotor;
		this.odo = odo;
		this.navi = new Navigation(odo, usSensor, usData);
		this.loc = new USLocalizer(odo, usSensor, usData, USLocalizer.LocalizationType.FALLING_EDGE);
		this.maxRange = 66;
	}
	

	public void startDetection()
	{
		LCD.clear(3);
		LCD.clear(4);
		LCD.drawString("Object Detecting...", 0, 3);
		while(true)
		{
			if(navi.blocked(4))	//check if an object is detected
			{
				LCD.clear(3);
				LCD.drawString("Object Detected", 0, 3);	//display object detected
				blockIdentify();							//identify the object
				try {
					Thread.sleep(1000);					//wait for user to see the result
				} catch (InterruptedException e) {
				}
				LCD.clear(3);
				LCD.drawString("Object Detecting...", 0, 3);
			}
		}
	}
	
	//start object searching
	//  robot should be placed at least 10cm from each wall, to avoid false detection of wall
	public void startSearching()
	{
		double y = 0;
		boolean detectRows = true;	// if false, search columns instead
		
		LCD.clear(3);
		LCD.clear(4);
		loc.doLocalization();					//do the localization
		LCD.drawString("Localization Done", 0, 3);
		try {
			Thread.sleep(1000);					//wait 2.5 sec for user to see the result
		} catch (InterruptedException e) {
		}
		LCD.clear(3);
		LCD.drawString("Searching...", 0, 3);
		
		//start searching through the whole board, see the top note for searching pattern
		// First, check on the 0 column
		//navi.turnTo(90, true);		// turn to the positive y-axis
		if(objectSeen())			// check if any object is seen within 60cm range
		{
			travelAndSearch();		//search till the end of row
			if(block)
				return;
			detectRows = false;
		}
		
		// Then, check for each row or column
		if(detectRows)
		{
			for( int i = 0; i < (numTravel + 2) && (!block); i++)
			{
				navi.travelTo(0, y);		// go to the current row for searching
				navi.turnTo(0, true);		// turn to the positive x-axis
				if(objectSeen())			// check if any object is seen within 60cm range
				{
					travelAndSearch();		//search till the end of row
				}
				
				//y += maxRange/numTravel;		//(double/int) will give a double
				y += this.tileLength/2;
			}
			y = 0;
			for( int i = 1; i < numTravel && (!block); i++)
			{
				y += maxRange/numTravel;		//(double/int) will give a double
				navi.travelTo(y, maxRange);		// go to the current row for searching
				navi.turnTo(270, true);		// turn to the positive x-axis
				if(objectSeen())			// check if any object is seen within 60cm range
				{
					travelAndSearch();		//search till the end of row
				}
			}
		}
		else
		{
			for( int i = 1; i < numTravel && (!block); i++)
			{
				y += maxRange/numTravel;		//(double/int) will give a double
				navi.travelTo(y, 0);		// go to the current column for searching
				navi.turnTo(90, true);		// turn to the positive y-axis
				if(objectSeen())			// check if any object is seen within 60cm range
				{
					travelAndSearch();		//search till the end of row
				}
				
			}
			y = 0;
			for( int i = 1; i < numTravel && (!block); i++)
			{
				y += maxRange/numTravel;		//(double/int) will give a double
				navi.travelTo(maxRange, y);		// go to the current column for searching
				navi.turnTo(180, true);		// turn to the positive y-axis
				if(objectSeen())			// check if any object is seen within 60cm range
				{
					travelAndSearch();		//search till the end of row
				}
			}
		}
		
		//if end without capture, display failure
		if(!block)
		{
			LCD.clear(3);
			LCD.clear(4);
			LCD.drawString("End", 0, 3);
			LCD.drawString("No target found", 0, 4);
		}
	}
	
	//travel forward and search for object
	private void travelAndSearch()
	{
		double oldX = odo.getX();	//keep the current position
		double oldY = odo.getY();
		while(!navi.blocked() && odo.getX() < maxRange && odo.getY() < maxRange)	//going forward until an object is detected or edge is reached
		{
			navi.setSpeeds(Navigation.FAST, Navigation.FAST);
		}
		if(odo.getX() < maxRange && odo.getY() < maxRange)	// if object is detected and edge is not reached
		{
			Sound.buzz();
			navi.stop();
			navi.setSpeeds(Navigation.VERYSLOW, Navigation.VERYSLOW);// slowly move closer to the block
			while(getColorID() == -1 || getColorID() == 7);	// to get rid of the false value -1 and 7 and see the true color value
			navi.stop();
			blockIdentify();			//check if the obstacle is a Styro block
			if(block)					//if object is a block
			{
				capture();				//capture the block to upper right corner
				Sound.beep();			//beep 3 times
				Sound.beep();
				Sound.beep();
				navi.stop();			//then stop
				LCD.clear(3);
				LCD.drawString("Finished", 0, 3);//Display finish text
				return;
			}
		}
		
		
		//if object is not a block or edge is reached
		try {
			Thread.sleep(1000);					
		} catch (InterruptedException e) {
		}
		navi.travelTo(0, oldY);	// go back to the start point of search
		return;
	}
	
	//capture the block and send it to upper right corner
	private void capture()
	{
		LCD.clear(3);
		LCD.drawString("Capturing...", 0, 3);
		
		navi.setSpeeds(-Navigation.FAST, -Navigation.FAST);// to leave enough distance to the block for turning back
		try {
			Thread.sleep(700);					
		} catch (InterruptedException e) {
		}
		navi.stop();
		
		//because our robot  catches block from back, we need to turn 180 degree first
		navi.turnTo(Odometer.fixDegAngle(odo.getAng() - 180), true);
		
		//catch the block with claw
		this.clawMotor.rotate(-200);
		//go to final corner
		navi.travelTo(maxRange, maxRange);
		
		// move around to make sure the block get to the goal
		navi.turnTo(Odometer.fixDegAngle(odo.getAng() - 180), true);
	}

	//identify and display the identified object
	private void blockIdentify()
	{	
		if(RGBwithinRange())	//colorRGB within color range of Styrofoam block AND colorID == the Styrofoam color id
		{
			block = true;
			LCD.clear(4);
			LCD.drawString("Block", 0, 4);
			Sound.beep();
		}
		else
		{
			float colorRGB[] = getColorRGB();
			//String realcolorId0 = ""+colorRGB[0]; // these were used to know RGB values of objects
			//String realcolorId1 = ""+colorRGB[1];
			//String realcolorId2 = ""+colorRGB[2];
			block = false;
			LCD.clear(4);
			LCD.clear(5);
			LCD.drawString("Not Block", 0, 4);
			//LCD.drawString(realcolorId0, 0, 5);
			//LCD.drawString(realcolorId1, 0, 6);
			//LCD.drawString(realcolorId2, 0, 7);
			Sound.beep();
			Sound.beep();
		}
	}
	
	//get the color sensor value in RGB mode
	private float[] getColorRGB()
	{
		do{
			colorValue.fetchSample(colorData, 0);
			
			//filter out unwanted values
			if((colorData[0] > 0.4 || colorData[1] > 0.4 || colorData[2] > 0.4) && filterControl < FILTER_OUT)
			{
				filterControl++;
			}
			else
			{
				filterControl = 0;
			}
		}while(filterControl < FILTER_OUT && filterControl > 0);
		
		return colorData;
	}
	
	//check if the detected RGB value is within range of the Styrofoam RGB value
	private boolean RGBwithinRange()
	{
		float colorRGB[] = getColorRGB();
		// the following conditions are observed by testing
		if(colorRGB[0] < colorRGB[1] && colorRGB[2] < colorRGB[1])
			return true;
		else
			return false;
	}
	
	//get one of the 8 color ID integers
	private int getColorID() // not really used in code but left in for posterity (except for stopping motors in front of object)
	{
		int color;
		do{
			float[] tempID = new float[colorID.sampleSize()];
			colorID.fetchSample(tempID, 0);
			color = (int)tempID[0];
			
			// 2 is the color id of dark blue
			// filter out spurious 2s
			if( color == 2  && filterControl < FILTER_OUT)
			{
				filterControl++;
			}
			else
			{
				filterControl = 0;
			}
		}while(filterControl < FILTER_OUT && filterControl > 0);
		
		return color;
	}
	
	//return true if the us value is smaller than 60, which means an object present on the row
	private boolean objectSeen()
	{
		float distance;
		do{
			usSensor.fetchSample(usData, 0);
			distance = usData[0];
			
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
		
		return distance < 60;
	}
	
}