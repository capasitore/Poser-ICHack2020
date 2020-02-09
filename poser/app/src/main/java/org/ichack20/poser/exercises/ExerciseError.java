package org.ichack20.poser.exercises;

public enum ExerciseError {
  ELBOW_ERROR("Keep your arms straight!");

  private final String errorMessage;

  ExerciseError(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Override
  public String toString() {
    return errorMessage;
  }
}
