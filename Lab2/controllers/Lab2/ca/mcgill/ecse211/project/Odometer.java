package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.BASE_WIDTH;
import static ca.mcgill.ecse211.project.Resources.WHEEL_RAD;
import static ca.mcgill.ecse211.project.Resources.leftMotor;
import static ca.mcgill.ecse211.project.Resources.rightMotor;
import static simlejos.ExecutionController.waitUntilNextStep;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The odometer class keeps track of the robot's (x, y, theta) position.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 * @author Younes Boubekeur
 * @author Olivier St-Martin Cormier
 */
public class Odometer implements Runnable {
  
  /** The x-axis position in meters. */
  private volatile double x;
  
  /** The y-axis position in meters. */
  private volatile double y;
  
  /** The orientation (heading) in degrees. */
  private volatile double theta;
  
  /** The instantaneous change in position (dx, dy, dTheta). */
  private static double[] deltaPosition = new double[3];

  // Thread control tools
  
  /**  Fair lock for concurrent writing. */
  private static Lock lock = new ReentrantLock(true);
  
  /** Indicates if a thread is trying to reset any position parameters. */
  private volatile boolean isResetting = false;

  /** Lets other threads know that a reset operation is over. */
  private Condition doneResetting = lock.newCondition();

  /** The singleton odometer instance. */
  private static Odometer odo;

  // Motor-related variables
  /** The previous motor tacho counts (from previous iteration of while loop). */
  private static int[] prevTacho = new int[2];
  /** The current motor tacho counts. */
  private static int[] currTacho = new int[2];
  
  private static final int LEFT = 0;
  private static final int RIGHT = 1;


  /**
   * This is the default constructor of this class. It initiates all motors and variables once. It
   * cannot be accessed externally.
   */
  private Odometer() {
    setXyt(0, 0, 0);
  }

  /**
   * Returns the Odometer Object. Use this method to obtain an instance of Odometer.
   * 
   * @return the Odometer Object
   */
  public static synchronized Odometer getOdometer() {
    if (odo == null) {
      odo = new Odometer();
    }
    return odo;
  }

  /**
   * This method is where the logic for the odometer will run.
   */
  public void run() {
    // Reset motor tacho counts to zero
    leftMotor.resetTachoCount();
    rightMotor.resetTachoCount();
    
    while (true) {
      // Update current and previous tacho counts 
      prevTacho[LEFT] = currTacho[LEFT];
      prevTacho[RIGHT] = currTacho[RIGHT];
      currTacho[LEFT] = leftMotor.getTachoCount();
      currTacho[RIGHT] = rightMotor.getTachoCount();

      // Method updates the deltaPosition
      updateDeltaPosition(prevTacho, currTacho, theta, deltaPosition);

      // Method updates odometer values
      updateOdometerValues();
      
      // Method prints odometer information to the console
      printPosition();
      
      // Waits until the next physics step to run the next iteration of the loop
      waitUntilNextStep();
    }
  }
  
  /**
   * Updates the robot deltaPosition (x, y, theta) given the motor tacho counts.
   * 
   * @param prev the previous tacho counts of the motors
   * @param curr the current tacho counts of the motors
   * @param theta the current heading (angle) of the robot
   * @param deltas the deltaPosition array (x, y, theta)
   */
  public static void updateDeltaPosition(int[] prev, int[] curr, double theta, double[] deltas) {
    // Calculates changes in x, y, theta based on current and previous tachocounts, then assign them
    // Computes wheel displacements and saves these tacho counts for the next iteration 
    double dtheta = 0;
    double dx = 0;
    double dy = 0;
    double distL = Math.PI * WHEEL_RAD * (curr[LEFT] - prev[LEFT]) / 180.0;
    double distR = Math.PI * WHEEL_RAD * (curr[RIGHT] - prev[RIGHT]) / 180.0;
    dtheta = (distL - distR) / BASE_WIDTH; // computes change in heading in radians
    // (distL + distR) * 0.5 computes vehicle displacement
    dx = ((distL + distR) * 0.5) * Math.sin(dtheta + (theta * Math.PI / 180)); 
    dy = ((distL + distR) * 0.5) * Math.cos(dtheta + (theta * Math.PI / 180)); 

    // Sets deltas
    deltas[0] = dx;
    deltas[1] = dy;
    deltas[2] = (180.0 * dtheta) / Math.PI; // convert to degrees
  }
  
  /**
   * Increments the current values of the x, y and theta instance variables using deltaPositions.
   * Useful for odometry.
   */
  public void updateOdometerValues() {
    lock.lock();
    isResetting = true;
    try {
      x += deltaPosition[0]; 
      y += deltaPosition[1];  
      theta += (deltaPosition[2] % 360.0);
      isResetting = false;
      doneResetting.signalAll(); // Let the other threads know we are done resetting
    } finally {
      lock.unlock();
    }
  }

  /**
   * Prints odometer information to the console.
   */
  public void printPosition() {
    lock.lock();
    System.out.println("Print odometer x: " + (int) (x * 100)
        + "       y: " + (int) (y * 100) 
        + "        theta: " + (int) theta); 
    lock.unlock();
  }
  
  /**
   * Returns the Odometer data.
   * 
   * <p>{@code odoData[0] = x, odoData[1] = y; odoData[2] = theta;}
   * 
   * @return the odometer data.
   */
  public double[] getXyt() {
    double[] position = new double[3];
    lock.lock();
    try {
      while (isResetting) { // If a reset operation is being executed, wait until it is over.
        doneResetting.await(); // Using await() is lighter on the CPU than simple busy wait.
      }
      position[0] = x;
      position[1] = y;
      position[2] = theta;
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }

    return position;
  }

  /**
   * Overwrites the values of x, y and theta. Use for odometry correction.
   * 
   * @param x the value of x
   * @param y the value of y
   * @param theta the value of theta in degrees
   */
  public void setXyt(double x, double y, double theta) {
    lock.lock();
    isResetting = true;
    try {
      this.x = x;
      this.y = y;
      this.theta = theta;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Overwrites x. Use for odometry correction.
   * 
   * @param x the value of x
   */
  public void setX(double x) {
    lock.lock();
    isResetting = true;
    try {
      this.x = x;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Overwrites y. Use for odometry correction.
   * 
   * @param y the value of y
   */
  public void setY(double y) {
    lock.lock();
    isResetting = true;
    try {
      this.y = y;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Overwrites theta. Use for odometry correction.
   * 
   * @param theta the value of theta
   */
  public void setTheta(double theta) {
    lock.lock();
    isResetting = true;
    try {
      this.theta = theta;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

}
