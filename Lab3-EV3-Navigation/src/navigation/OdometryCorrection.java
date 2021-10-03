/* 
 * OdometryCorrection.java
 */
//team 7
package navigation;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

public class OdometryCorrection extends Thread {
	EV3ColorSensor sensor;
	private static double tileLength = 30.48;
	private static final long CORRECTION_PERIOD = 20;
	private Odometer odometer;
	private Integer counter = 1;

	// constructor
	public OdometryCorrection(Odometer odometer) {
	//	this.sensor = csensor;
		this.odometer = odometer;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		sensor.setFloodlight(true);
		SensorMode mode = sensor.getRedMode();	
		while (true) {
			correctionStart = System.currentTimeMillis();
			float[] color = new float[mode.sampleSize()];
			mode.fetchSample(color, 0);
			// put your correction code here
			if (color[0] * 10 < 3) {
				if (counter == 1){
					odometer.setY(tileLength);
				} else if (counter == 2){
					odometer.setY(2*tileLength);
				} else if (counter == 3){
					odometer.setY(3*tileLength);
				} else if (counter == 4){
					odometer.setX(tileLength);
				} else if (counter == 5){
					odometer.setX(2*tileLength);
				} else if (counter == 6){
					odometer.setX(3*tileLength);
				} else if (counter == 7){
					odometer.setY(3*tileLength);
				} else if (counter == 8){
					odometer.setY(2*tileLength);
				} else if (counter == 9){
					odometer.setY(tileLength);
				} else if (counter == 10){
					odometer.setX(3*tileLength);
				} else if (counter == 11){
					odometer.setX(2*tileLength);
				} else if (counter == 12){
					odometer.setX(tileLength);
					counter = 0;
				}
				counter = counter + 1;
			}
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}