package ca.mcgill.ecse211.project;

/* TODO Using a star import (one that ends with *) will lead to a CheckStyle warning.
 * When you are finished, before you submit, right-click all files that have this import in Eclipse
 * and select Source > Organize imports, or use Cmd/Ctrl-Shift-O. Finally, delete this comment. */
import static ca.mcgill.ecse211.project.Resources.*;

import simlejos.ExecutionController;

/**
 * Main class of the program.
 */
public class Main {

  /**
   * The main entry point.
   * 
   * @param args not used
   */
  public static void main(String[] args) {
    System.out.println("Starting Lab 0 demo"); // prints to Webots console
    
    // Array of one element where the sample is stored
     float[] touchSensorSample = new float[touchSensor.sampleSize()]; 
     System.out.println("Allocated buffer");
  
    init();
    
    forward();
    
    // Keep moving forward while the touch sensor is not pressed
    while (!isPressed(touchSensorSample)) {
      // Need to explicitly perform the physics step
      ExecutionController.performPhysicsStep();
      
      /* Update touchSensorSample by fetching a sample from the touch sensor provider and storing
       * in touchSensorSample at index 0.
       * 
       * Equivalent pseudocode: touchSensorSample = touchSensor.fetchSample() */
      touchSensor.fetchSample(touchSensorSample, 0);
    }
    
    stopMotors();
    
    // Need to perform a final step for the stop to be registered
    ExecutionController.performPhysicsStep();
    
    System.exit(0);
  }
  
  /**
   * Returns true if the touch sensor sample indicates the sensor was pressed.
   * 
   * @param touchSensorSample touch sensor sample, a one-element float array
   * @return true if the touch sensor sample indicates the sensor was pressed
   */
  public static boolean isPressed(float[] touchSensorSample) {
    System.out.println(touchSensorSample[0]);
    return touchSensorSample[0] == 1.0;
  }
  
  /**
   * Helper method used to initialize program.
   */
  private static void init() {
    // Need to define how many threads are synchronized to simulation steps
    ExecutionController.setNumberOfParties(NUMBER_OF_THREADS);
    
    // Wait 1 second before moving to make sure everything has settled
    for (int i = 0; i < 10; i++) {
      ExecutionController.performPhysicsStep();
    }
  }
  
  /**
   * Moves the robot forward with FORWARD_SPEED. In future labs, a method like this should be
   * placed in its own class along with other related methods.
   */
  public static void forward() {
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
    leftMotor.forward();
    rightMotor.forward();
  }
  
  /**
   * Stops the motors.
   */
  public static void stopMotors() {
    leftMotor.stop();
    rightMotor.stop();
  }
  
}
