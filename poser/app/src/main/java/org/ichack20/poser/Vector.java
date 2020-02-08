package org.ichack20.poser;

public class Vector extends Point {

  public Vector(Point start, Point end) {
    super(end.getX() - start.getX(), end.getY() - start.getY());
  }

  public float getLength() {
    return (float) Math.sqrt(getX() * getX() + getY() * getY());
  }

  public float dotProductWith(Vector vector) {
    return getX() * vector.getX() + getY() * vector.getY();
  }

  public float detWith(Vector vector) {
    return getX() * vector.getY() - getY() * vector.getX();
  }
}
