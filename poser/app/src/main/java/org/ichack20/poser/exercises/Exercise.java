package org.ichack20.poser.exercises;

import java.util.HashMap;
import java.util.Map;
import org.ichack20.poser.Pose;
import org.ichack20.poser.TextToSpeech;

public abstract class Exercise {

  public enum Move {
    UP, DOWN
  }

  public enum Orientation {
    FRONT, SIDE
  }

  protected int reps;

  public Map<ExerciseError, Integer> getErrors() {
    return errors;
  }

  protected Map<ExerciseError, Integer> errors;

  public int getReps() {
    return reps;
  }

  public Exercise() {
    this.reps = 0;
    this.errors = new HashMap<>();
  }

  // Can (maybe) use to substitute checks for error in exercise classes
  public boolean trackError(boolean errorCondition, ExerciseError error,
      boolean inError) {
    if (errorCondition) {
      if (!inError) {
        inError = true;
        if (errors.containsKey(error)) {
          int count = errors.get(error);
          count++;
          errors.put(error, count);
        } else {
          errors.put(error, 1);
        }
      }
    } else {
      if (inError) {
        inError = false;
      }
    }
    return inError;
  }

  public abstract Orientation getOrientation();
  public abstract void update(Pose pose, TextToSpeech textToSpeech);
}
