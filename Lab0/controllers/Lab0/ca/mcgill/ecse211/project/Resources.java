package ca.mcgill.ecse211.project;

import simlejos.hardware.motor.Motor;
import simlejos.hardware.port.SensorPort;
import simlejos.hardware.sensor.EV3TouchSensor;
import simlejos.robotics.RegulatedMotor;
import simlejos.robotics.SampleProvider;

/**
 * This class is used to define static resources in one place for easy access and to avoid 
 * cluttering the rest of the codebase. All resources can be imported at once by placing this
 * line of code at the top of your file:
 * 
 * <p>{@code import static ca.mcgill.ecse211.project.Resources.*;}
 */
public class Resources {
  
  // Constants
  
  /** The number of threads used in the program. */
  public static final int NUMBER_OF_THREADS = 1;
  
  /** The speed with which the robot moves forward, in deg/s. */
  public static final int FORWARD_SPEED = 300;
  
  // Hardware resources
  
  /** The touch sensor sample provider. */
  public static final SampleProvider touchSensor = new EV3TouchSensor(SensorPort.S2).getTouchMode();

  /** The left motor. */ 
  public static final RegulatedMotor leftMotor = Motor.D;
  
  /** The right motor. */
  public static final RegulatedMotor rightMotor = Motor.A;
  
}
