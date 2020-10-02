package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.ACCELERATION;
import static ca.mcgill.ecse211.project.Resources.BASE_WIDTH;
import static ca.mcgill.ecse211.project.Resources.FORWARD_SPEED;
import static ca.mcgill.ecse211.project.Resources.ROTATE_SPEED;
import static ca.mcgill.ecse211.project.Resources.TILE_SIZE;
import static ca.mcgill.ecse211.project.Resources.TIMEOUT_PERIOD;
import static ca.mcgill.ecse211.project.Resources.WHEEL_RAD;
import static ca.mcgill.ecse211.project.Resources.leftMotor;
import static ca.mcgill.ecse211.project.Resources.rightMotor;
import static simlejos.ExecutionController.sleepFor;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class SquareDriver {

  /** The length of the size of the square in tiles. Ranges from 3 to 6 (inclusive) */
  public static final int SQUARE_LENGTH = 14; 

  /**
   * Drives the robot in a square of SQUARE_LENGTH. It is to be run in parallel with the odometer
   * thread to allow
   * testing its functionality.
   */
  public static void drive() {
    // spawn a new Thread to avoid this method blocking
    new Thread(() -> {
      // reset the motors
      stopMotors();
      setAcceleration(ACCELERATION);

      // Sleep for 3 seconds
      sleepFor(TIMEOUT_PERIOD);

      for (int i = 0; i < 4; i++) {
        setSpeed(FORWARD_SPEED);
        moveStraightFor(SQUARE_LENGTH);

        // turn 90 degrees clockwise
        setSpeed(ROTATE_SPEED);
        turnBy(90.0);
      }
      stopMotors();
    }).start();
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

  /**
   * Stops both motors.
   */
  public static void stopMotors() {
    leftMotor.stop();
    rightMotor.stop();
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
   * Sets the acceleration of both motors.
   * 
   * @param acceleration the acceleration in degrees per second squared
   */
  public static void setAcceleration(int acceleration) {
    leftMotor.setAcceleration(acceleration);
    rightMotor.setAcceleration(acceleration);
  }

}
