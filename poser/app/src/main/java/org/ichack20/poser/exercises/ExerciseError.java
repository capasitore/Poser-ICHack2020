package org.ichack20.poser.exercises;

public enum ExerciseError {

  ELBOW_FLEX_ERROR("Keep your arms straight!"),
  SHOULDER_MOVE_ERROR("Don't move your upper arm!"),
  HIP_FLEX_ERROR("Don't move your hips!");

  private final String errorMessage;

  ExerciseError(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Override
  public String toString() {
    return errorMessage;
  }
}
