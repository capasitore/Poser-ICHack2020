package org.ichack20.poser;

public class Vector extends Point {

  public Vector(Point start, Point end) {
    super(end.getX() - start.getX(), end.getY() - start.getY());
  }

  public double getLength() {
    return Math.sqrt(getX() * getX() + getY() * getY());
  }

  public double dotProductWith(Vector vector) {
    return getX() * vector.getX() + getY() * vector.getY();
  }

  public double detWith(Vector vector) {
    return getX() * vector.getY() - getY() * vector.getX();
  }
}
