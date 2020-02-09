package org.ichack20.poser.exercises;

import java.util.HashMap;
import java.util.Map;
import org.ichack20.poser.Pose;
import org.ichack20.poser.Pose.Angle;

public class LateralRaise implements Exercise {

  private final Orientation orientation;
  private final static int START_SHOULDER_ANGLE = 20;
  private final static int END_SHOULDER_ANGLE = 80;
  private final static int ERROR_ELBOW_ANGLE = 160;
  private boolean in_error_elbow_angle = false;
  private int reps;
  // TODO: idea: add badReps (for easier angle margins)
  private Move prevMove = Move.DOWN;
  private Map<ExerciseError, Integer> errors;

  public LateralRaise() {
    this.orientation = Orientation.FRONT;
    this.reps = 0;
    this.errors = new HashMap<>();
  }

  public String error_msg(ExerciseError error) {
    switch (error) {
      case ELBOW_ERROR: return "Keep your arms straight!";
    }
    return "";
  }

  @Override
  public void update(Pose pose) {
    if (pose.getAngle(Angle.L_SHOULDER) > END_SHOULDER_ANGLE
        && pose.getAngle(Angle.R_SHOULDER) > END_SHOULDER_ANGLE) {
      if (prevMove == Move.UP) {
        prevMove = Move.DOWN;
      }
    }
    if (pose.getAngle(Angle.L_SHOULDER) < START_SHOULDER_ANGLE
        && pose.getAngle(Angle.R_SHOULDER) < START_SHOULDER_ANGLE) {
      if (prevMove == Move.DOWN) {
        prevMove = Move.UP;
        reps++;
      }
    }
    if (pose.getAngle(Angle.L_ELBOW) < ERROR_ELBOW_ANGLE || pose.getAngle(Angle.R_ELBOW) < ERROR_ELBOW_ANGLE) {
      if (!in_error_elbow_angle) {
        in_error_elbow_angle = true;
        if (errors.containsKey(ExerciseError.ELBOW_ERROR)) {
          int count = errors.get(ExerciseError.ELBOW_ERROR);
          count++;
          errors.put(ExerciseError.ELBOW_ERROR, count);
        } else {
          errors.put(ExerciseError.ELBOW_ERROR, 0);
        }
      }
    } else {
      if (in_error_elbow_angle) {
        in_error_elbow_angle = false;
      }
    }
  }
  enum Move {
    UP, DOWN
  }
}