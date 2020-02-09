package org.ichack20.poser;

import android.app.Activity;
import android.graphics.Bitmap;
import com.edvard.poseestimation.ImageClassifier;
import com.edvard.poseestimation.ImageClassifierFloatInception;

public class PoseEstimator {
  private final ImageClassifier imageClassifier;

  public PoseEstimator(Activity activity) {
    this(activity, 192, 192, 96, 96, "model.tflite", 4);
  }

  public PoseEstimator(Activity activity, int imageSizeX, int imageSizeY, int outputW, int outputH,
      String modelPath, int numBytesPerChannel) {
    this.imageClassifier = ImageClassifierFloatInception.Companion.create(activity, imageSizeX,
        imageSizeY, outputW, outputH, modelPath, numBytesPerChannel);
    this.imageClassifier.initTflite(true);
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
