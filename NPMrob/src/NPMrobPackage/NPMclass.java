package NPMrobPackage;

import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;


/**
 * Example leJOS EV3 Project with an ant build file
 *
 */
public class NPMclass {

	public static void main(String[] args) {
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		TextLCD lcd = ev3.getTextLCD();
		Keys keys = ev3.getKeys();
		
		lcd.drawString("reussite", 4, 4);
		keys.waitForAnyPress();
	}
	
}