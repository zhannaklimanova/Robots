package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.colorSensor;
import static ca.mcgill.ecse211.project.Resources.colorSensor2;
import static ca.mcgill.ecse211.project.Resources.leftMotor;
import static ca.mcgill.ecse211.project.Resources.rightMotor;
import static ca.mcgill.ecse211.project.Resources.TILE_SIZE;
import static ca.mcgill.ecse211.project.Resources.BASE_WIDTH;
import static ca.mcgill.ecse211.project.Resources.FORWARD_SPEED;
import static ca.mcgill.ecse211.project.Resources.WHEEL_RAD;;

public class LightLocalizer {

  public static final int SQUARE_LENGTH = 14; 
  public static float[] colorSensorData = new float[colorSensor.sampleSize()]; // leftColorSensor 
  public static float[] colorSensorData2 = new float[colorSensor2.sampleSize()]; // rightColorSensor
  public static float colorSensorValuePrev = colorSensorData[0];
  public static float colorSensor2ValuePrev = colorSensorData[0];
  public static float differenceValue = 15; // 15 is good value w/ my previous version of code
  
  public static void localize() {
    lightLocalizer();
    System.out.println("STOPPED1");
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
//  rotate
    turnBy(90);    
    System.out.println("ROTATED");
    lightLocalizer();
    System.out.println("MOVED AGAIN");
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
//  rotate
    turnBy(-90);    
    System.out.println("ROTATED2");
    moveStraightFor(-0.1);
    lightLocalizer();
    setSpeed(0);
//    moveforward a bit
  }
  
  public static void lightLocalizer() {
    colorSensor.fetchSample(colorSensorData, 0);
    colorSensor2.fetchSample(colorSensorData2, 0);
    colorSensorValuePrev = colorSensorData[0];
    colorSensor2ValuePrev = colorSensorData2[0];
    boolean leftSide = false;
    boolean rightSide = false;
    
    if (leftMotor.getSpeed() == 0 && rightMotor.getSpeed() == 0) {
      System.out.println("HAAAAA");
      leftMotor.setSpeed(FORWARD_SPEED);
      rightMotor.setSpeed(FORWARD_SPEED);
    }
    boolean b = true;
    while (b) {
//      if (leftSide && rightSide) {
//        System.out.println("PEN");
//        break;
//      }
      leftMotor.forward();
      rightMotor.forward();
      colorSensor.fetchSample(colorSensorData, 0);
      colorSensor2.fetchSample(colorSensorData2, 0);

      System.out.println("TESTTTTTTT " + (colorSensorValuePrev - colorSensorData[0]));
      System.out.println("TESTTTTTTT2 " + (colorSensor2ValuePrev - colorSensorData2[0]));
      
      
      if (Math.abs(colorSensor2ValuePrev - colorSensorData2[0]) > differenceValue) {
//        rightMotor.stop();
        System.out.println("STOP");
        rightMotor.setSpeed(0);
        System.out.println("RIGHTSIDEEE IS " + leftSide);
        rightSide = true; // found the line 
      }
      if (Math.abs(colorSensorValuePrev - colorSensorData[0]) > differenceValue) {
        // setSpeed(0);
        System.out.println("STOP");
//        leftMotor.stop();
        leftMotor.setSpeed(0);
        
        System.out.println("LEFTSPEED IS " + leftSide);
        leftSide = true;
      }
      colorSensorValuePrev = colorSensorData[0];
      colorSensor2ValuePrev = colorSensorData2[0];
      System.out.println("colorSensorData " + colorSensorData[0]);
      System.out.println("colorSensorData2 " + colorSensorData2[0]);
      if (leftSide == true && rightSide == true) {
        setSpeed(FORWARD_SPEED);
        b = false;
      }
    }
  }
  
  /**
   * Sets the speed of both motors to the same values.
   * 
   * @param speed the speed in degrees per second
   */
  public static void setSpeed(int speed) {
    setSpeeds(speed, speed);
  }

  /**
   * Sets the speed of both motors to different values.
   * 
   * @param leftSpeed the speed of the left motor in degrees per second
   * @param rightSpeed the speed of the right motor in degrees per second
   */
  public static void setSpeeds(int leftSpeed, int rightSpeed) {
    leftMotor.setSpeed(leftSpeed);
    rightMotor.setSpeed(rightSpeed);
  }
  
  /**
   * Moves the robot straight for the given distance.
   * 
   * @param distance in feet (tile sizes), may be negative
   */
  public static void moveStraightFor(double distance) {
    leftMotor.rotate(convertDistance(distance * TILE_SIZE), true);
    rightMotor.rotate(convertDistance(distance * TILE_SIZE), false);
  }
  
  /**
   * Turns the robot by a specified angle. Note that this method is different from 
   * {@code Navigation.turnTo()}. For example, if the robot is facing 90 degrees, 
   * calling {@code turnBy(90)} will make the robot turn to 180 degrees, but calling 
   * {@code Navigation.turnTo(90)} should do nothing (since the robot is already at 90 degrees).
   * 
   * @param angle the angle by which to turn, in degrees
   */
  public static void turnBy(double angle) {
    leftMotor.rotate(convertAngle(angle), true);
    rightMotor.rotate(-convertAngle(angle), false);
  }
  
  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance the input distance in meters
   * @return the wheel rotations necessary to cover the distance
   */
  public static int convertDistance(double distance) {
    double circumference = 2 * Math.PI * WHEEL_RAD; // circumference of the wheel in meters
    // number of rotations the wheel has to make to cross a certain distance
    int totalWheelRotations = (int) (((distance * 360) / circumference)); 
    return totalWheelRotations;
  }

  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the 
   * robot by that angle.
   * 
   * @param angle the input angle in degrees
   * @return the wheel rotations necessary to rotate the robot by the angle
   */
  public static int convertAngle(double angle) { 
    // this is the distance the wheels will have to turn to make the rotation angle
    int totalWheelRotations = convertDistance(Math.PI * BASE_WIDTH * angle / 360.0); 
    return totalWheelRotations;
  }
  
}
