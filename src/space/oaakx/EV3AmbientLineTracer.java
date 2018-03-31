package space.oaakx;

import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;

public class EV3AmbientLineTracer implements LineTracer {
	@Override
	public void run() {
		// Initialize motors, sensors and screen
		RegulatedMotor leftMotor = Motor.C;
		RegulatedMotor rightMotor = Motor.B;
		EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		TextLCD lcd = ev3.getTextLCD();
		
		// Initialize sampleFetcher
		float ambientSample[];
		SensorMode ambientMode = colorSensor.getAmbientMode();
		ambientSample = new float[ambientMode.sampleSize()];

		// Hard-coded values
		leftMotor.setSpeed(400);
		rightMotor.setSpeed(400);
		float lower = 0.04f; // needs adjustment
		float upper = 0.07f; // needs adjustment
		
		// Start moving the robot
		leftMotor.backward(); // backward because of gears
		rightMotor.backward();
		
		while (true) {
			ambientMode.fetchSample(ambientSample, 0);

			// Output sample data
			lcd.clear();
			lcd.drawString(String.valueOf(ambientSample[0]), 1, 3);
			
			// Correct direction
			if (lower <= ambientSample[0] && ambientSample[0] <= upper) {
				leftMotor.backward();
				rightMotor.backward();
			}
			else if (ambientSample[0] < lower) { 
				leftMotor.backward();
				rightMotor.stop();
			}
			else if (ambientSample[0] > upper) { 
				leftMotor.stop();
				rightMotor.backward();
			}
			
			// Allow for some time before self-correcting
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
		
//		leftMotor.stop();
//		rightMotor.stop();
//		colorSensor.close();
	}
}
