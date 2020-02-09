package org.ichack20.poser;

import android.app.Activity;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import java.io.File;
import java.io.IOException;

public class SpeechRecognition implements RecognitionListener {

  public interface SpeechRecognitionListener {
    void onStart();
    void onPause();
    void onStop();
  }

  private static final String KEYPHRASE_START = "start";
  private static final String KEYPHRASE_PAUSE = "pause";
  private static final String KEYPHRASE_STOP = "stop";

  private final SpeechRecognitionListener listener;
  private final SpeechRecognizer speechRecognizer;

  public SpeechRecognition(Activity activity,
      SpeechRecognitionListener listener) throws IOException {
    this.listener = listener;

    Assets assets = new Assets(activity);
    File assetDir = assets.syncAssets();

    this.speechRecognizer = SpeechRecognizerSetup.defaultSetup()
        .setAcousticModel(new File(assetDir, "en-us-ptm"))
        .setDictionary(new File(assetDir, "cmudict-en-us.dict"))
        .setRawLogDir(assetDir)
        .getRecognizer();

    this.speechRecognizer.addListener(this);

    //this.speechRecognizer.addKeyphraseSearch("keyword", KEYPHRASE_START);
    //this.speechRecognizer.addKeyphraseSearch("keyword", KEYPHRASE_PAUSE);
    this.speechRecognizer.addKeyphraseSearch("keyword", KEYPHRASE_STOP);
  }

  public void start() {
    speechRecognizer.startListening("keyword");
  }

  public void stop() {
    speechRecognizer.cancel();
    speechRecognizer.stop();
  }

  @Override
  public void onBeginningOfSpeech() {

  }

  @Override
  public void onEndOfSpeech() {

  }

  private void processResult(Hypothesis hypothesis) {
    if (hypothesis != null) {
      switch (hypothesis.getHypstr()) {
        case KEYPHRASE_START:
          listener.onStart();
          break;

        case KEYPHRASE_PAUSE:
          listener.onPause();
          break;

        case KEYPHRASE_STOP:
          listener.onStop();
          break;
      }
    }
  }

  @Override
  public void onPartialResult(Hypothesis hypothesis) {
    processResult(hypothesis);
  }

  @Override
  public void onResult(Hypothesis hypothesis) {
    processResult(hypothesis);
  }

  @Override
  public void onError(Exception e) {
    e.printStackTrace();
  }

  @Override
  public void onTimeout() {

  }
}
