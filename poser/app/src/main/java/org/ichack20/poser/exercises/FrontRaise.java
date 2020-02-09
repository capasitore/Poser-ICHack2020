package org.ichack20.poser.exercises;

import org.ichack20.poser.Pose;
import org.ichack20.poser.Pose.Angle;
import org.ichack20.poser.TextToSpeech;

public class FrontRaise extends Exercise {

  // ALWAYS RIGHT HAND IN FRONT OF THE CAMERA!!!

  private final static int ERROR_ELBOW_ANGLE = 150;
  private final static int END_SHOULDER_ANGLE = 290;
  private final static int START_SHOULDER_ANGLE = 340;
  private final static int ERROR_HIP_ANGLE_FORWARD = 160;
  private final static int ERROR_HIP_ANGLE_BACKWARD = 210;

  private boolean in_error_elbow_angle = false;
  private boolean in_error_hip_angle = false;
  private Move prevMove = Move.UP;

  @Override
  public Orientation getOrientation() {
    return Orientation.SIDE;
  }

  @Override
  public void update(Pose pose, TextToSpeech textToSpeech) {
    if (pose.getAngle(Angle.R_SHOULDER) < END_SHOULDER_ANGLE
        && pose.getAngle(Angle.R_SHOULDER) > 120) { // > 120 to prevent overflow to 0
      if (prevMove == Move.UP) {
        prevMove = Move.DOWN;
      }
    }

    if (pose.getAngle(Angle.R_SHOULDER) > START_SHOULDER_ANGLE) {
      if (prevMove == Move.DOWN) {
        prevMove = Move.UP;
        reps++;
      }
    }

    // Keep track of flexing arm and add errors
    in_error_elbow_angle = trackError(pose.getAngle(Angle.R_ELBOW)
        < ERROR_ELBOW_ANGLE, ExerciseError.ELBOW_FLEX_ERROR,
        in_error_elbow_angle);

    // Keep track of hip flex and add errors
    in_error_hip_angle = trackError(pose.getAngle(Angle.R_HIP)
        < ERROR_HIP_ANGLE_FORWARD || pose.getAngle(Angle.R_HIP)
        > ERROR_HIP_ANGLE_BACKWARD, ExerciseError.HIP_FLEX_ERROR,
        in_error_hip_angle);
  }
}