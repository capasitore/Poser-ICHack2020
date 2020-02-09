package org.ichack20.poser.exercises;

import org.ichack20.poser.Pose;
import org.ichack20.poser.Pose.Angle;

public class FrontRaise extends Exercise {

  // ALWAYS RIGHT HAND IN FRONT OF THE CAMERA!!!

  private boolean in_error_elbow_angle = false;
  private boolean in_error_hip_angle = false;
  private Move prevMove = Move.UP;

  @Override
  public Orientation getOrientation() {
    return Orientation.SIDE;
  }

  @Override
  public void update(Pose pose) {

    if (pose.getAngle(Angle.R_SHOULDER) > END_SHOULDER_ANGLE) {
      if (prevMove == Move.UP) {
        prevMove = Move.DOWN;
      }
    }

    if (pose.getAngle(Angle.R_SHOULDER) < START_SHOULDER_ANGLE_SIDE) {
      if (prevMove == Move.DOWN) {
        prevMove = Move.UP;
        reps++;
      }
    }

    // Keep track of flexing arm and add errors
    if (pose.getAngle(Angle.R_ELBOW) < ERROR_ELBOW_ANGLE) {
      if (!in_error_elbow_angle) {
        in_error_elbow_angle = true;
        if (errors.containsKey(ExerciseError.ELBOW_FLEX_ERROR)) {
          int count = errors.get(ExerciseError.ELBOW_FLEX_ERROR);
          count++;
          errors.put(ExerciseError.ELBOW_FLEX_ERROR, count);
        } else {
          errors.put(ExerciseError.ELBOW_FLEX_ERROR, 1);
        }
      }
    } else {
      if (in_error_elbow_angle) {
        in_error_elbow_angle = false;
      }
    }

    // Keep track of hip flex and add errors
    if (pose.getAngle(Angle.R_HIP) < ERROR_HIP_ANGLE_FORWARD || pose.getAngle(Angle.R_HIP) > ERROR_HIP_ANGLE_BACKWARD) {
      if (!in_error_hip_angle) {
        in_error_hip_angle = true;
        if (errors.containsKey(ExerciseError.HIP_FLEX_ERROR)) {
          int count = errors.get(ExerciseError.HIP_FLEX_ERROR);
          count++;
          errors.put(ExerciseError.HIP_FLEX_ERROR, count);
        } else {
          errors.put(ExerciseError.HIP_FLEX_ERROR, 1);
        }
      }
    } else {
      if (in_error_hip_angle) {
        in_error_hip_angle = false;
      }
    }
  }
}