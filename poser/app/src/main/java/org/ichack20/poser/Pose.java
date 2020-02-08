package org.ichack20.poser;


import java.security.acl.NotOwnerException;

public class Pose {

  Point[] points = new Point[Node.values().length];

  public Pose(float[][] points) {
    for (int i = 0; i <= points.length; i++) {
      this.points[i] = new Point(points[0][i], points[1][i]);
    }
  }

  private float getAngle(Point first, Point middle, Point last) {
    //TODO
    return 0;
  }

  public float getAngle(Node first, Node middle, Node last) {
    //TODO
    return 0;
  }

  public float getAngle(Angle angle) {
    return getAngle(angle.getFirst(), angle.getMiddle(), angle.getLast());
  }


  enum Angle {
    L_SHOULDER(Node.L_HIP, Node.L_SHOULDER, Node.L_ELBOW),
    R_SHOULDER(Node.R_ELBOW, Node.R_SHOULDER, Node.R_HIP),
    L_ELBOW(Node.L_SHOULDER, Node.L_ELBOW, Node.L_WRIST),
    R_ELBOW(Node.R_WRIST, Node.R_ELBOW, Node.R_SHOULDER);

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

  enum Node {
    TOP,
    NECK,
    L_SHOULDER,
    L_ELBOW,
    L_WRIST,
    R_SHOULDER,
    R_ELBOW,
    R_WRIST,
    L_HIP,
    L_KNEE,
    L_ANKLE,
    R_HIP,
    R_KNEE,
    R_ANKLE,
    BACKGROUND
  }
}
