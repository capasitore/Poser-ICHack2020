package org.ichack20.poser.exercises;

import org.ichack20.poser.Pose;
import org.ichack20.poser.Pose.Angle;

public class BicepsCurl extends Exercise {

  private boolean in_error_hip_angle = false;
  private boolean in_error_shoulder_angle = false;
  private Move prevMove = Move.UP;

  @Override
  public Orientation getOrientation() {
    return Orientation.SIDE;
  }

  @Override
  public void update(Pose pose) {

    double leftElbow = pose.getAngle(Angle.L_ELBOW);
    double rightElbow = pose.getAngle(Angle.R_ELBOW);
    double leftShoulder = pose.getAngle(Angle.L_SHOULDER);
    double rightShoulder = pose.getAngle(Angle.R_SHOULDER);

    if (leftElbow < END_ELBOW_ANGLE && rightElbow < END_ELBOW_ANGLE) {
      if (prevMove == Move.UP) {
        prevMove = Move.DOWN;
      }
    }

    if (leftElbow > START_ELBOW_ANGLE && rightElbow > START_ELBOW_ANGLE) {
      if (prevMove == Move.DOWN) {
        prevMove = Move.UP;
        reps++;
      }
    }

    in_error_shoulder_angle = trackError(leftShoulder, rightShoulder,
        ERROR_SHOULDER_ANGLE_FORWARD, ERROR_SHOULDER_ANGLE_BACKWARD,
        ExerciseError.SHOULDER_MOVE_ERROR, in_error_shoulder_angle);

    if (leftShoulder > ERROR_SHOULDER_ANGLE_FORWARD && leftShoulder < ERROR_SHOULDER_ANGLE_BACKWARD
        || rightShoulder > ERROR_SHOULDER_ANGLE_FORWARD && rightShoulder < ERROR_SHOULDER_ANGLE_BACKWARD) {
      if (!in_error_shoulder_angle) {
        in_error_shoulder_angle = true;
        if (errors.containsKey(ExerciseError.SHOULDER_MOVE_ERROR)) {
          int count = errors.get(ExerciseError.SHOULDER_MOVE_ERROR);
          count++;
          errors.put(ExerciseError.SHOULDER_MOVE_ERROR, count);
        } else {
          errors.put(ExerciseError.SHOULDER_MOVE_ERROR, 1);
        }
      }
    } else {
      if (in_error_shoulder_angle) {
        in_error_shoulder_angle = false;
      }
    }

    // Kee[ track of moving hips and add errors
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
