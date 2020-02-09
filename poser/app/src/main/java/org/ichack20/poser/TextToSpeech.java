package org.ichack20.poser;

import android.content.Context;
import android.speech.tts.TextToSpeech.OnInitListener;

public class TextToSpeech {

  private final android.speech.tts.TextToSpeech textToSpeech;

  public TextToSpeech(Context context, OnInitListener listener) {
    this.textToSpeech = new android.speech.tts.TextToSpeech(context, listener);
  }

  public void speak(String text) {
    textToSpeech.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH,
        null, null);
  }
}
