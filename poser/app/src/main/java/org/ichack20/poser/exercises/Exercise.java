package org.ichack20.poser.exercises;

import java.util.HashMap;
import java.util.Map;
import org.ichack20.poser.Pose;

public abstract class Exercise {

  protected int reps;
  protected Map<ExerciseError, Integer> errors;

  public Exercise() {
    this.reps = 0;
    this.errors = new HashMap<>();
  }

  public abstract Orientation getOrientation();
  public abstract void update(Pose pose);
}
