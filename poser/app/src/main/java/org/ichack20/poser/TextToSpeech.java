package org.ichack20.poser;

import android.content.Context;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

public class TextToSpeech {

  private final android.speech.tts.TextToSpeech textToSpeech;

  private Runnable afterSpeakingCompleted;

  public TextToSpeech(Context context, OnInitListener listener) {
    this.textToSpeech = new android.speech.tts.TextToSpeech(context, listener);
    this.textToSpeech.setOnUtteranceProgressListener(
        new UtteranceProgressListener() {
          @Override
          public void onStart(String s) {

          }

          @Override
          public void onDone(String s) {
            if (afterSpeakingCompleted != null) {
              afterSpeakingCompleted.run();
            }
          }

          @Override
          public void onError(String s) {

          }
        });
  }

  public void speak(String text) {
    speak(text, null);
  }

  public void speak(String text, Runnable afterSpeakingCompleted) {
    String utteranceId = null;
    if (afterSpeakingCompleted != null) {
      this.afterSpeakingCompleted = afterSpeakingCompleted;
      utteranceId = "utter";
    }

    textToSpeech.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD,
        null, utteranceId);
  }
}
