package org.ichack20.poser.exercises;

import org.ichack20.poser.Pose;

public class BicepsCurl extends Exercise {

  private final static int START_ELBOW_ANGLE = 165;
  private final static int END_ELBOW_ANGLE = 50;
  private final static int ERROR_SHOULDER_ANGLE = 30;
  private final static boolean in_error_shoulder_angle = false;

  @Override
  public Orientation getOrientation() {
    return Orientation.SIDE;
  }

  @Override
  public void update(Pose pose) {

  }
}
