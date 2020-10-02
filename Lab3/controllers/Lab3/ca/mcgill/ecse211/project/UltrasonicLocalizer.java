package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static simlejos.ExecutionController.*;

public class UltrasonicLocalizer {
  
  /** The distance remembered by the filter() method. */
  private static int prevDistance;
  /** The number of invalid samples seen by filter() so far. */
  private static int invalidSampleCount;
  /** Buffer (array) to store US samples. */
  private static float[] usData = new float[usSensor.sampleSize()];
  private static float[] averageData = new float[5];
  /** Arbitrary constant to find the local maximum of the measured distance. */
  private static final int d = 30;
  
  private static int counter = 0;
  
  private static double firstAngle = 0;
  private static double secondAngle = 0;
  
  private static boolean firstAngleExists = false;
  private static boolean secondAngleExists = false;
  private static double averageAngle = 0;
  private static boolean case1 = false; // robot is facing away from wall (values are > 1)
  private static boolean case2 = false; // robot is facing wall (values are < 1)
  private static boolean startingRotations = false;
  
  /**
   * Autonomously orientates the robot using the ultrasonic sensor.
   */
  public static void localize() {
    readUsDistance();
    if (usData[0] > 1) {
      System.out.println("CASE FACING AWAY FROM WALL");
      ultrasonicLocalizer();
      averageAngle = (firstAngle + secondAngle) / 2; 
      // System.out.println("averageAngle" + averageAngle);
      double robotOrientation = secondAngle - (averageAngle + 135.0);
      // System.out.println("robotOrientation" + robotOrientation);
      turnBy(-robotOrientation);
    }
    else {
      System.out.println("CASE FACING WALL");
      ultrasonicLocalizer2();

      averageAngle = (firstAngle + secondAngle) / 2; 
      // System.out.println("averageAngle" + averageAngle);
      double robotOrientation = secondAngle - (averageAngle + 135.0);
      // System.out.println("robotOrientation" + robotOrientation);
      turnBy(-(robotOrientation + 180));
    }
//    averageAngle = (firstAngle + secondAngle) / 2; 

    
  }
  
  public static void ultrasonicLocalizer() {
    stopMotors();
    setAcceleration(ACCELERATION);

    leftMotor.forward();
    rightMotor.backward();
//    for (int i = 50; i > 0; i--) {
//      waitUntilNextStep();
//    }
    filterValues();
    if (usData[0] < 1) { // if its facing the wall
      System.out.println("CASE 1");
    }
    else if (usData[0] > 1) {
      while (counter != 2) { // while robot hasn't found both wall crossing angles
//        System.out.println(readUsDistance());
        setSpeeds(ROTATE_SPEED, ROTATE_SPEED);
        if (firstAngleExists == false && counter != 1) {
          leftMotor.forward();
          rightMotor.backward();
//          System.out.println("COllecting   " + usData[0]);
        }
//        System.out.println("FELICIA" + usData[0]);
        if (firstAngleExists == false && readUsDistance() < d) {
          firstAngle = odometer.getXyt()[2];
          counter++;
          firstAngleExists = !firstAngleExists;
//          System.out.println("Angle " + firstAngle + " AT location " + usData[0]);
          leftMotor.backward();
          rightMotor.forward();
        }
        else if (secondAngleExists == false && readUsDistance() < d && firstAngleExists == true 
            && Math.abs(firstAngle - odometer.getXyt()[2]) > 30) {
          secondAngle = odometer.getXyt()[2];
          counter++;
          secondAngleExists = !secondAngleExists;
//          System.out.println("I got it too" + usData[0]);
        }
//        System.out.println("COunter  " + counter );
      }
    }
  }
    
  public static void ultrasonicLocalizer2() {
    stopMotors();
    setAcceleration(ACCELERATION);
    System.out.println("2222222");
    leftMotor.backward();
    rightMotor.forward();
//    for (int i = 50; i > 0; i--) {
//      waitUntilNextStep();
//    }
    filterValues();
    if (usData[0] > 1) { // if its facing the wall
      System.out.println("CASE 1");
    } else if (usData[0] < 1) {
      while (counter != 2) { // while robot hasn't found both wall crossing angles
        // System.out.println(readUsDistance());
        setSpeeds(ROTATE_SPEED, ROTATE_SPEED);
        if (firstAngleExists == false && counter != 1) {
          leftMotor.backward();
          rightMotor.forward();
          // System.out.println("COllecting   " + readUsDistance());
        }
        // System.out.println("FELICIA" + usData[0]);
        if (firstAngleExists == false && readUsDistance() > d) {
          firstAngle = odometer.getXyt()[2];
          counter++;
          firstAngleExists = !firstAngleExists;
          // System.out.println("Angle " + firstAngle + " AT location " + usData[0]);
          leftMotor.forward();
          rightMotor.backward();
        } else if (secondAngleExists == false && readUsDistance() > d && firstAngleExists == true
            && Math.abs(firstAngle - odometer.getXyt()[2]) > 30) {
          secondAngle = odometer.getXyt()[2];
          counter++;
          secondAngleExists = !secondAngleExists;
          // System.out.println("I got it too" + usData[0]);
        }
        // System.out.println("COunter  " + counter);
      }
    }

  }
  
  public static void filterValues() { // filter out too high and too low values 
    readUsDistance();
    readUsDistance();
    readUsDistance();
    readUsDistance();
    readUsDistance();
  }
  
  /** Returns the filtered distance between the US sensor and an obstacle in cm. */
  public static int readUsDistance() {
    usSensor.fetchSample(usData, 0);
    averageData[4] = averageData[3];
    averageData[3] = averageData[2];
    averageData[2] = averageData[1];
    averageData[1] = averageData[0];
    averageData[0] = usData[0];
    float totalData = 0;
    for (int i = 0; i < averageData.length; i++) {
      totalData += averageData[i];
    }
    totalData = totalData / averageData.length;
    // extract from buffer, cast to int, and filter
    return filter((int) (totalData * 100.0));
  }
  /**
   * Rudimentary filter - toss out invalid samples corresponding to null signal.
   * 
   * @param distance raw distance measured by the sensor in cm
   * @return the filtered distance in cm
   */
  
  static int filter(int distance) {
    if (distance >= MAX_SENSOR_DIST && invalidSampleCount < INVALID_SAMPLE_LIMIT) {
      // bad value, increment the filter value and return the distance remembered from before
      invalidSampleCount++;
      return prevDistance;
    } else {
      if (distance < MAX_SENSOR_DIST) {
        invalidSampleCount = 0; // reset filter and remember the input distance.
      }
      prevDistance = distance;
      return distance;
    }
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
   * Sets the speed of both motors to different values.
   * 
   * @param leftSpeed the speed of the left motor in degrees per second
   * @param rightSpeed the speed of the right motor in degrees per second
   */
  public static void setSpeeds(int leftSpeed, int rightSpeed) {
    // TODO
    leftMotor.setSpeed(leftSpeed);
    rightMotor.setSpeed(rightSpeed);
  }
  
  /**
   * Sets the acceleration of both motors.
   * 
   * @param acceleration the acceleration in degrees per second squared
   */
  public static void setAcceleration(int acceleration) {
    // TODO
    leftMotor.setAcceleration(acceleration);
    rightMotor.setAcceleration(acceleration);
  }
  
  /**
   * Stops both motors.
   */
  public static void stopMotors() {
    leftMotor.stop();
    rightMotor.stop();
  }
  
  /**
   * Computes the angle to be added to the heading reported by the odometer
   * to orient the robot correctly.
   * @param alpha first falling-edge angle
   * @param beta second falling-edge angle
   * @return delta angle to be added to the heading of the odometer
   */
  private static double deltaAngle(double alpha, double beta) {
    if (alpha < beta) {
      return 45 - (alpha - beta) / 2;
    }
    return 225 - (alpha - beta) / 2;
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
}



//package ca.mcgill.ecse211.project;
//
//import static ca.mcgill.ecse211.project.Resources.*;
//import static simlejos.ExecutionController.*;
//
//public class UltrasonicLocalizer {
//  
//  /** The distance remembered by the filter() method. */
//  private static int prevDistance;
//  /** The number of invalid samples seen by filter() so far. */
//  private static int invalidSampleCount;
//  /** Buffer (array) to store US samples. */
//  private static float[] usData = new float[usSensor.sampleSize()];
//  private static float[] averageData = new float[5];
//  /** Arbitrary constant to find the local maximum of the measured distance. */
//  private static final int d = 30;
//  
//  private static int counter = 0;
//  
//  private static double firstAngle = 0;
//  private static double secondAngle = 0;
//  
//  private static boolean firstAngleExists = false;
//  private static boolean secondAngleExists = false;
//  private static double averageAngle = 0;
//  private static boolean case1 = false; // robot is facing away from wall (values are > 1)
//  private static boolean case2 = false; // robot is facing wall (values are < 1)
//  private static boolean startingRotations = false;
//  
//  /**
//   * Autonomously orientates the robot using the ultrasonic sensor.
//   */
//  public static void localize() {
//    ultrasonicLocalizer();
//    averageAngle = (firstAngle + secondAngle) / 2; 
//    System.out.println("averageAngle" + averageAngle);
//    double robotOrientation = secondAngle - (averageAngle + 135.0);
//    System.out.println("robotOrientation" + robotOrientation);
//    turnBy(-robotOrientation);
//  }
//  
//  public static void ultrasonicLocalizer() { 
//    stopMotors();
//    setAcceleration(ACCELERATION);
//    readUsDistance();
//
//    System.out.println("distance placed BEFORE" + usData[0]);
//    if (usData[0] < 1) { // Case where robot is facing the wall
//      System.out.println("distance read" + readUsDistance()); // 3 
//      System.out.println("distance placed" + usData[0]); // 0.097
//      System.out.println("Facing wall");
//
//      leftMotor.backward();
//      rightMotor.forward();
//      System.out.println("distance read ajfkdl;" + readUsDistance()); // 112
//      System.out.println("distance placed fdjkal;" + usData[0]); // 2.19
////      for (int i = 50; i > 0; i--) {
////        waitUntilNextStep();
////      }
////      filterValues();
////      System.out.println("CASE 1");
////      while (counter != 2) {
//
////        if (firstAngleExists == false && counter != 1) {
////          leftMotor.backward();
////          rightMotor.forward();
////        }
////        if (firstAngleExists == false && readUsDistance() < d) {
////          counter++; 
////          firstAngleExists = !firstAngleExists;
////          leftMotor.forward();
////          rightMotor.backward();
////        }
////        else if (secondAngleExists == false && readUsDistance() < d && firstAngleExists == true
////            && Math.abs(firstAngle) - odometer.getXyt()[2] > d) {
////            secondAngle = odometer.getXyt()[2];
////            counter++;
////            secondAngleExists = !secondAngleExists;
////        }
////      }
//    }
//    else if (usData[0] > 1) { // Case where robot is facing away from wall
//      System.out.println("distance read" + readUsDistance()); // 112
//      System.out.println("distance placed" + usData[0]); // 2.19
//      System.out.println("Facing away from wall");
//
//      leftMotor.forward();
//      rightMotor.backward();
//      System.out.println("distance read fjdaksl;" + readUsDistance()); // 112
//      System.out.println("distance placed fjkdsal;" + usData[0]); // 2.19
//      System.out.println("SKY");
////      for (int i = 50; i > 0; i--) {
////        waitUntilNextStep();
////      }
////      filterValues();
////      while (counter != 2) { // while robot hasn't found both wall crossing angles
////        System.out.println(readUsDistance());
////        setSpeeds(ROTATE_SPEED, ROTATE_SPEED);
////        if (firstAngleExists == false && counter != 1) {
////          leftMotor.forward();
////          rightMotor.backward();
////          System.out.println("COllecting   " + usData[0]);
////        }
//////        System.out.println("FELICIA" + usData[0]);
////        if (firstAngleExists == false && readUsDistance() < d) {
////          firstAngle = odometer.getXyt()[2];
////          counter++;
////          firstAngleExists = !firstAngleExists;
////          System.out.println("Angle " + firstAngle + " AT location " + usData[0]);
////          leftMotor.backward();
////          rightMotor.forward();
////        }
////        else if (secondAngleExists == false && readUsDistance() < d && firstAngleExists == true 
////            && Math.abs(firstAngle - odometer.getXyt()[2]) > d) {
////          secondAngle = odometer.getXyt()[2];
////          counter++;
////          secondAngleExists = !secondAngleExists;
////          System.out.println("I got it too" + usData[0]);
////        }
////        System.out.println("COunter  " + counter );
////      }
//    }
//   
//
//  }
//  
//  public static void filterValues() { // filter out too high and too low values 
//    readUsDistance();
//    readUsDistance();
//    readUsDistance();
//    readUsDistance();
//    readUsDistance();
//  }
//  
//  /** Returns the filtered distance between the US sensor and an obstacle in cm. */
//  public static int readUsDistance() {
//    usSensor.fetchSample(usData, 0);
//    averageData[4] = averageData[3];
//    averageData[3] = averageData[2];
//    averageData[2] = averageData[1];
//    averageData[1] = averageData[0];
//    averageData[0] = usData[0];
//    float totalData = 0;
//    for (int i = 0; i < averageData.length; i++) {
//      totalData += averageData[i];
//    }
//    totalData = totalData / averageData.length;
//    // extract from buffer, cast to int, and filter
//    return filter((int) (totalData * 100.0));
//  }
//  /**
//   * Rudimentary filter - toss out invalid samples corresponding to null signal.
//   * 
//   * @param distance raw distance measured by the sensor in cm
//   * @return the filtered distance in cm
//   */
//  
//  static int filter(int distance) {
//    if (distance >= MAX_SENSOR_DIST && invalidSampleCount < INVALID_SAMPLE_LIMIT) {
//      // bad value, increment the filter value and return the distance remembered from before
//      invalidSampleCount++;
//      return prevDistance;
//    } else {
//      if (distance < MAX_SENSOR_DIST) {
//        invalidSampleCount = 0; // reset filter and remember the input distance.
//      }
//      prevDistance = distance;
//      return distance;
//    }
//  }
//  
//  /**
//   * Converts input distance to the total rotation of each wheel needed to cover that distance.
//   * 
//   * @param distance the input distance in meters
//   * @return the wheel rotations necessary to cover the distance
//   */
//  public static int convertDistance(double distance) {
//    double circumference = 2 * Math.PI * WHEEL_RAD; // circumference of the wheel in meters
//    // number of rotations the wheel has to make to cross a certain distance
//    int totalWheelRotations = (int) (((distance * 360) / circumference)); 
//    return totalWheelRotations;
//  }
//
//  /**
//   * Converts input angle to the total rotation of each wheel needed to rotate the 
//   * robot by that angle.
//   * 
//   * @param angle the input angle in degrees
//   * @return the wheel rotations necessary to rotate the robot by the angle
//   */
//  public static int convertAngle(double angle) { 
//    // this is the distance the wheels will have to turn to make the rotation angle
//    int totalWheelRotations = convertDistance(Math.PI * BASE_WIDTH * angle / 360.0); 
//    return totalWheelRotations;
//  }
//  
//  /**
//   * Sets the speed of both motors to different values.
//   * 
//   * @param leftSpeed the speed of the left motor in degrees per second
//   * @param rightSpeed the speed of the right motor in degrees per second
//   */
//  public static void setSpeeds(int leftSpeed, int rightSpeed) {
//    // TODO
//    leftMotor.setSpeed(leftSpeed);
//    rightMotor.setSpeed(rightSpeed);
//  }
//  
//  /**
//   * Sets the acceleration of both motors.
//   * 
//   * @param acceleration the acceleration in degrees per second squared
//   */
//  public static void setAcceleration(int acceleration) {
//    // TODO
//    leftMotor.setAcceleration(acceleration);
//    rightMotor.setAcceleration(acceleration);
//  }
//  
//  /**
//   * Stops both motors.
//   */
//  public static void stopMotors() {
//    leftMotor.stop();
//    rightMotor.stop();
//  }
//  
//  /**
//   * Computes the angle to be added to the heading reported by the odometer
//   * to orient the robot correctly.
//   * @param alpha first falling-edge angle
//   * @param beta second falling-edge angle
//   * @return delta angle to be added to the heading of the odometer
//   */
//  private static double deltaAngle(double alpha, double beta) {
//    if (alpha < beta) {
//      return 45 - (alpha - beta) / 2;
//    }
//    return 225 - (alpha - beta) / 2;
//  }
//  
//  /**
//   * Turns the robot by a specified angle. Note that this method is different from 
//   * {@code Navigation.turnTo()}. For example, if the robot is facing 90 degrees, 
//   * calling {@code turnBy(90)} will make the robot turn to 180 degrees, but calling 
//   * {@code Navigation.turnTo(90)} should do nothing (since the robot is already at 90 degrees).
//   * 
//   * @param angle the angle by which to turn, in degrees
//   */
//  public static void turnBy(double angle) {
//    leftMotor.rotate(convertAngle(angle), true);
//    rightMotor.rotate(-convertAngle(angle), false);
//  }
//  
//  
//}
