package org.ichack20.poser.exercises;

import java.util.HashMap;
import java.util.Map;
import org.ichack20.poser.Pose;

public abstract class Exercise {

  protected int reps;
  protected Map<ExerciseError, Integer> errors;
  final static int START_SHOULDER_ANGLE_FRONT = 20;
  final static int END_SHOULDER_ANGLE = 80;
  final static int ERROR_ELBOW_ANGLE = 160;
  final static int START_ELBOW_ANGLE = 165;
  final static int END_ELBOW_ANGLE = 50;
  final static int ERROR_SHOULDER_ANGLE_FORWARD = 30;
  final static int ERROR_SHOULDER_ANGLE_BACKWARD = 150;
  final static int ERROR_HIP_ANGLE_FORWARD = 160;
  final static int ERROR_HIP_ANGLE_BACKWARD = 200;
  final static int START_SHOULDER_ANGLE_SIDE = 15;

  public Exercise() {
    this.reps = 0;
    this.errors = new HashMap<>();
  }

  public boolean trackError(double e1, double e2, int limit1, int limit2, ExerciseError error, boolean in_error) {
    if (e1 > limit1 && e1 < limit2 || e2 > limit1 && e2 < limit2) {
      if (!in_error) {
        in_error = true;
        if (errors.containsKey(error)) {
          int count = errors.get(error);
          count++;
          errors.put(error, count);
        } else {
          errors.put(error, 1);
        }
      }
    } else {
      if (in_error) {
        in_error = false;
      }
    }
    return in_error;
  }

  public abstract Orientation getOrientation();
  public abstract void update(Pose pose);
}
