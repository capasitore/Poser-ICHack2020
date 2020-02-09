package org.ichack20.poser;

public class Pose {

  private static final double DEG_PER_RAD = 180 / Math.PI;

  private final Point[] points = new Point[Node.values().length];

  public Pose(float[][] points) {
    for (int i = 0; i <= points.length; i++) {
      this.points[i] = new Point(points[0][i], points[1][i]);
    }
  }

  private double getAngle(Point first, Point middle, Point last) {
    Vector firstVector = new Vector(first, middle);
    Vector secondVector = new Vector(middle, last);

    double dotProduct = firstVector.dotProductWith(secondVector);
    double det = firstVector.detWith(secondVector);

    double result_rad = Math.atan2(det, dotProduct);

    return result_rad * DEG_PER_RAD;
  }

  public double getAngle(Node first, Node middle, Node last) {
    return getAngle(points[first.ordinal()], points[middle.ordinal()], points[last.ordinal()]);
  }

  public double getAngle(Angle angle) {
    return getAngle(angle.getFirst(), angle.getMiddle(), angle.getLast());
  }

  public enum Angle {
    L_SHOULDER(Node.R_HIP, Node.R_SHOULDER, Node.R_ELBOW),
    R_SHOULDER(Node.L_ELBOW, Node.L_SHOULDER, Node.L_HIP),
    L_ELBOW(Node.R_SHOULDER, Node.R_ELBOW, Node.R_WRIST),
    R_ELBOW(Node.L_WRIST, Node.L_ELBOW, Node.L_SHOULDER),
    R_HIP(Node.L_KNEE, Node.L_HIP, Node.NECK);

    public Node getFirst() {
      return first;
    }

    public Node getMiddle() {
      return middle;
    }

    public Node getLast() {
      return last;
    }

    private final Node first;
    private final Node middle;
    private final Node last;

    Angle(Node first, Node middle, Node last) {
      this.first = first;
      this.middle = middle;
      this.last = last;
    }
  }

  public enum Node {
    TOP,
    NECK,
    R_SHOULDER,
    R_ELBOW,
    R_WRIST,
    L_SHOULDER,
    L_ELBOW,
    L_WRIST,
    R_HIP,
    R_KNEE,
    R_ANKLE,
    L_HIP,
    L_KNEE,
    L_ANKLE,
    BACKGROUND
  }
}
