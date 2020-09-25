package ca.mcgill.ecse211.project;

import simlejos.hardware.motor.Motor;
import simlejos.robotics.RegulatedMotor;

/**
 * Class for static resources (things that stay the same throughout the entire program execution),
 * like constants and hardware.
 * <br><br>
 * Use these resources in other files by adding this line at the top (see examples):<br><br>
 * 
 * {@code import static ca.mcgill.ecse211.project.Resources.*;}
 */
public class Resources {
  
  /** The wheel radius in meters. */
  public static final double WHEEL_RAD = 0.021; // 2.1 cm actual measurement in webots

  /** The robot width in meters. */
  public static final double BASE_WIDTH = 0.16; // 15.54 cm actual measurement in webots

  /** The speed at which the robot moves forward in degrees per second. */
  public static final int FORWARD_SPEED = 500; 

  /** The speed at which the robot rotates in degrees per second. */
  public static final int ROTATE_SPEED = 250; 

  /** The motor acceleration in degrees per second squared. */
  public static final int ACCELERATION = 3000; //

  /** Timeout period in milliseconds. */
  public static final int TIMEOUT_PERIOD = 3000;

  /** The tile size in meters. Note that 0.3048 m = 1 ft. */
  public static final double TILE_SIZE = 0.3048;

  /** The odometer. */
  public static Odometer odometer = Odometer.getOdometer();

  /** The left motor. */
  public static final RegulatedMotor leftMotor = Motor.A;

  /** The right motor. */
  public static final RegulatedMotor rightMotor = Motor.D;
  
}
