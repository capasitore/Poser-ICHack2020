package org.ichack20.poser;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import com.edvard.poseestimation.ImageClassifier;
import com.edvard.poseestimation.ImageClassifierFloatInception;

public class PoseEstimator {

  private final ImageClassifier imageClassifier;

  public PoseEstimator(ImageClassifier imageClassifier) {
    this.imageClassifier = imageClassifier;
  }

  public Pose processFrame(Bitmap bitmap) {
    try {
      imageClassifier.classifyFrame(bitmap);
      float[][] points = imageClassifier.getMPrintPointArray();

      if (points == null) {
        throw new RuntimeException("Result is null");
      }

      return new Pose(points);
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }
}
