package space.oaakx;

import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.port.SensorPort;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class EV3RGBLineTracer implements LineTracer {
	@Override
	public void run() {
		// Initialize motors, sensors and screen
		RegulatedMotor leftMotor = Motor.C;
		RegulatedMotor rightMotor = Motor.B;
		EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		TextLCD lcd = ev3.getTextLCD();
		
		// Initialize sampleFetcher
		SensorMode rgbMode = colorSensor.getRGBMode();
		float rgbSample[] = new float[rgbMode.sampleSize()];
		float red = 0f;
		float blue = 0f;
		float green = 0f;
		
		// Hard-coded values
		float lower = 0.030f;
		float upper = 0.090f;
		float mid = 0f;
		int leftSpeed = 0;
		int rightSpeed = 0;
		int maxSpeed = 700;
		int normSpeed = 600;
		leftMotor.setSpeed(400); // this doesn't really matter
		rightMotor.setSpeed(400);

		// Start moving the robot
		leftMotor.backward(); // backward because of gears
		rightMotor.backward();
		
		while (true) {
			// Fetch sensor data
			rgbMode.fetchSample(rgbSample, 0);
			red = rgbSample[0];
			green = rgbSample[1];
			blue = rgbSample[2];
			
			// Output sample data
			lcd.clear();
			lcd.drawString(String.valueOf(red), 1, 1);
//			lcd.drawString(String.valueOf(green), 1, 2);
//			lcd.drawString(String.valueOf(blue), 1, 3);
			
			// Correct motor speeds
			mid = (lower + upper) / 2;
			
			if (red > 0.060f && green < 0.060f && blue < 0.060f) {
				break;
			}
			else if (red <= mid - 0.005f) { // allow for some level of error
				leftSpeed = (int) (normSpeed + (maxSpeed-normSpeed)*(mid-red)/(mid-lower));
				leftSpeed = Math.min(Math.max(leftSpeed, 0), maxSpeed);
				rightSpeed = (int) (0 + normSpeed*(red-lower)/(mid-lower));
				rightSpeed = Math.min(Math.max(rightSpeed, 0), maxSpeed);
				
				// Another function for motor speed
//				leftSpeed = maxSpeed;
//				rightSpeed = (int) (maxSpeed*(red-lower)/(mid-lower));
//				rightSpeed = Math.min(Math.max(rightSpeed, 0), maxSpeed);
			}
			else if (mid + 0.005f <= red) {
				leftSpeed = (int) (0 + normSpeed*(upper-red)/(upper-mid));
				leftSpeed = Math.min(Math.max(leftSpeed, 0), maxSpeed);
				rightSpeed = (int) (normSpeed + (maxSpeed-normSpeed)*(red-mid)/(upper-mid));
				rightSpeed = Math.min(Math.max(rightSpeed, 0), maxSpeed);
				
				// Another function for motor speed
//				rightSpeed = maxSpeed;
//				leftSpeed = (int) (maxSpeed*(upper-red)/(upper-mid));
//				leftSpeed = Math.min(Math.max(leftSpeed, 0), maxSpeed);
			}
			else {
				leftSpeed = normSpeed;
				rightSpeed = normSpeed;
				
				// Another function for motor speed
//				leftSpeed = maxSpeed;
//				rightSpeed = maxSpeed;
			}
			
			// Update speeds
			leftMotor.setSpeed(leftSpeed);
			rightMotor.setSpeed(rightSpeed);
			
			// Sometimes after setSpeed(0) motors would stop completely
			leftMotor.backward();
			rightMotor.backward();
			
			// Output data for debugging
			lcd.drawString(String.valueOf(leftMotor.isStalled()), 1, 2);
			lcd.drawString(String.valueOf(rightMotor.isStalled()), 1, 3);
			lcd.drawString(String.valueOf(leftSpeed), 1, 4);
			lcd.drawString(String.valueOf(rightSpeed), 1, 5);
			
			// Allow for some time before self-correcting
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {}
		}
		
		leftMotor.stop();
		rightMotor.stop();
		colorSensor.close();
	}
	
}
