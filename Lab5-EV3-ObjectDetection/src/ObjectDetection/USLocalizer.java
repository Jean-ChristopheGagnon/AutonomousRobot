package ObjectDetection;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static float ROTATION_SPEED = 100;
	public static final float d = 28;	//values need to be modified
	public static final float k = 1;	//

	private final int FILTER_OUT = 20;
	private int filterControl;
	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private Navigation navi;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.navi = new Navigation(odo, usSensor, usData);
		this.filterControl = 0;
	}
	
	public void doLocalization() {
		double[] pos = {0,0,0};
		double angleA, angleB, dH;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			navi.setSpeeds(ROTATION_SPEED,-ROTATION_SPEED);	//rotate clockwise
			while(getFilteredData() < 50){}
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > (d+k)){}
			angleA = odo.getAng();
			while(getFilteredData() > (d-k)){}
			angleA = 0.5*(angleA+odo.getAng()); 
			
			// switch direction and wait until it sees no wall
			navi.setSpeeds(-ROTATION_SPEED,ROTATION_SPEED);	//rotate counterclockwise
			while(getFilteredData() < 50){}
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > (d+k)){}
			angleB = odo.getAng();
			while(getFilteredData() > (d-k)){}
			angleB = 0.5*(angleB+odo.getAng());
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			if(angleA > angleB)
				dH = 225 - 0.5*(angleA+angleB);
			else
				dH = 45 - 0.5*(angleA+angleB);
			//dH = 45 - 0.5*(angleA+angleB);
			pos[2] = odo.getAng() + dH;
			// update the odometer position (example to follow:)
			odo.setPosition(pos, new boolean [] {true, true, true});
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			//
			// FILL THIS IN
			//
			
			// rotate the robot 
			navi.setSpeeds(ROTATION_SPEED,-ROTATION_SPEED);	//rotate clockwise
						
			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > (d+k));
			angleA = odo.getAng();
			while(getFilteredData() > (d-k));
			angleA = 0.5*(angleA+odo.getAng());
			Sound.beep();

			// goes away from the local maximum
			while(getFilteredData() < (d-k));
			while(getFilteredData() >= (d-k));
			
			// keep rotating until the robot sees no wall, then latch the angle
			while(getFilteredData() < (d-k));
			angleB = odo.getAng();
			while(getFilteredData() < (d+k));
			angleB = 0.5*(angleB+odo.getAng());
			Sound.beep();
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			if(angleA > angleB)
				dH = 225 - 0.5*(angleA+angleB);
			else
				dH = 45 - 0.5*(angleA+angleB);
			pos[2] = odo.getAng() + dH;
			odo.setPosition(pos, new boolean [] {true, true, true});

		}
		navi.turnTo(0, true);
		try {
			Thread.sleep(1000);					//wait for user to see the result
		} catch (InterruptedException e) {
		}
		navi.travelTo(11, 11);
		try {
			Thread.sleep(1000);					//wait for user to see the result
		} catch (InterruptedException e) {
		}
		navi.turnTo(0, true);
		try {
			Thread.sleep(1000);					//wait for user to see the result
		} catch (InterruptedException e) {
		}
		pos[2] = 0;
		odo.setPosition(pos, new boolean [] {true, true, true});
		Sound.buzz();
	}
	
	private float getFilteredData() {
		float distance;
		do{
			usSensor.fetchSample(usData, 0);
			distance = usData[0] * 100;
			
			//limit the max distance to be 50
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
