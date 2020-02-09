package org.ichack20.poser.exercises;

import org.ichack20.poser.Pose;
import org.ichack20.poser.Pose.Angle;
import org.ichack20.poser.TextToSpeech;

public class LateralRaise extends Exercise {

  private final static int ERROR_ELBOW_ANGLE = 160;
  private final static int START_SHOULDER_ANGLE_FRONT = 45;
  private final static int END_SHOULDER_ANGLE = 100;

  private boolean in_error_elbow_angle = false;
  private Move prevMove = Move.UP;

  @Override
  public Orientation getOrientation() {
    return Orientation.FRONT;
  }

  @Override
  public void update(Pose pose, TextToSpeech textToSpeech) {
    if (pose.getAngle(Angle.L_SHOULDER) > END_SHOULDER_ANGLE
        && pose.getAngle(Angle.R_SHOULDER) > END_SHOULDER_ANGLE) {
      if (prevMove == Move.UP) {
        prevMove = Move.DOWN;
      }
    }

    if (pose.getAngle(Angle.L_SHOULDER) < START_SHOULDER_ANGLE_FRONT
        && pose.getAngle(Angle.R_SHOULDER) < START_SHOULDER_ANGLE_FRONT) {
      if (prevMove == Move.DOWN) {
        prevMove = Move.UP;
        reps++;
        textToSpeech.speak(reps + "");
      }
    }

    // Keep track of flexing arm and add errors
    in_error_elbow_angle = trackError(pose.getAngle(Angle.L_ELBOW)
            < ERROR_ELBOW_ANGLE || pose.getAngle(Angle.R_ELBOW)
            < ERROR_ELBOW_ANGLE,
        ExerciseError.ELBOW_FLEX_ERROR, in_error_elbow_angle);
  }
}
