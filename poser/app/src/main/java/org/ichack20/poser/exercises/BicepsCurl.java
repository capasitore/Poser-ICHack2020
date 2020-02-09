package org.ichack20.poser.exercises;

import org.ichack20.poser.Pose;
import org.ichack20.poser.Pose.Angle;
import org.ichack20.poser.TextToSpeech;

public class BicepsCurl extends Exercise {

  // ALWAYS RIGHT HAND IN FRONT OF THE CAMERA!!!

  private final static int START_ELBOW_ANGLE = 165;
  private final static int END_ELBOW_ANGLE = 50;
  private final static int ERROR_SHOULDER_ANGLE_FORWARD = 330;
  private final static int ERROR_SHOULDER_ANGLE_BACKWARD = 30;
  private final static int ERROR_HIP_ANGLE_FORWARD = 160;
  private final static int ERROR_HIP_ANGLE_BACKWARD = 200;

  private boolean in_error_hip_angle = false;
  private boolean in_error_shoulder_angle = false;
  private Move prevMove = Move.UP;

  @Override
  public Orientation getOrientation() {
    return Orientation.SIDE;
  }

  @Override
  public void update(Pose pose, TextToSpeech textToSpeech) {
    double leftElbow = pose.getAngle(Angle.L_ELBOW);
    double rightElbow = pose.getAngle(Angle.R_ELBOW);
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

        textToSpeech.speak(reps + "");
      }
    }

    // Keep track of moving upper arm and add errors
    in_error_shoulder_angle = trackError(
        rightShoulder < ERROR_SHOULDER_ANGLE_FORWARD
            && rightShoulder > ERROR_SHOULDER_ANGLE_BACKWARD,
        ExerciseError.SHOULDER_MOVE_ERROR, in_error_shoulder_angle);

    if (in_error_shoulder_angle) {
      textToSpeech.speak(ExerciseError.SHOULDER_MOVE_ERROR.toString());
    }

    // Kee[ track of moving hips and add errors
    in_error_hip_angle = trackError(pose.getAngle(Angle.R_HIP)
            < ERROR_HIP_ANGLE_FORWARD || pose.getAngle(Angle.R_HIP)
            > ERROR_HIP_ANGLE_BACKWARD,
        ExerciseError.HIP_FLEX_ERROR, in_error_hip_angle);

    if (in_error_hip_angle) {
      textToSpeech.speak(ExerciseError.HIP_FLEX_ERROR.toString());
    }
  }
}
