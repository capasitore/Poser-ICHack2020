package org.ichack20.poser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.edvard.poseestimation.CameraActivity;
import com.edvard.poseestimation.R;
import org.ichack20.poser.exercises.Exercise;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.exercises_picker);
  }

  private void startExercise(String exercise) {
    Intent intent = new Intent(this, CameraActivity.class);
    intent.putExtra("exercise", exercise);

    startActivity(intent);
  }

  public void onFrontClick(View view) {
    startExercise("front");
  }

  public void onCurlClick(View view) {
    startExercise("curl");
  }

  public void onLateralClick(View view) {
    startExercise("lateral");
  }
}
