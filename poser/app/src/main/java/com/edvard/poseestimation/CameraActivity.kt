/*
 * Copyright 2018 Zihua Zeng (edvard_hua@live.com), Lang Feng (tearjeaker@hotmail.com)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edvard.poseestimation

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import org.ichack20.poser.SpeechRecognition
import org.ichack20.poser.TextToSpeech
import org.ichack20.poser.exercises.BicepsCurl
import org.ichack20.poser.exercises.Exercise
import org.ichack20.poser.exercises.FrontRaise
import org.ichack20.poser.exercises.LateralRaise

import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import kotlin.system.exitProcess

/**
 * Main `Activity` class for the Camera app.
 */
class CameraActivity : Activity() {

  private val mLoaderCallback = object : BaseLoaderCallback(this) {
    override fun onManagerConnected(status: Int) {
      when (status) {
        LoaderCallbackInterface.SUCCESS -> isOpenCVInit = true
        LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION -> {
        }
        LoaderCallbackInterface.INIT_FAILED -> {
        }
        LoaderCallbackInterface.INSTALL_CANCELED -> {
        }
        LoaderCallbackInterface.MARKET_ERROR -> {
        }
        else -> {
          super.onManagerConnected(status)
        }
      }
    }
  }

  private var speechRecognition: SpeechRecognition? = null
  var textToSpeech: TextToSpeech? = null

  private var cameraFragment: Camera2BasicFragment? = null
  var exercise: Exercise? = null

  inner class ResourceInitTask : AsyncTask<Void, Void, SpeechRecognition?>() {
    private var progressDialog = ProgressDialog(this@CameraActivity)

    override fun onPreExecute() {
      progressDialog.setMessage("Loading, please wait...")
      progressDialog.show()
    }

    override fun doInBackground(vararg p0: Void?): SpeechRecognition? {
      return try {
        SpeechRecognition(this@CameraActivity, SpeechListener())
      } catch (exception: Exception) {
        exception.printStackTrace()
        null
      }
    }

    override fun onPostExecute(result: SpeechRecognition?) {
      speechRecognition = result

      if (speechRecognition == null) {
        progressDialog.hide()
        val builder = AlertDialog.Builder(this@CameraActivity)
        builder.setTitle("Speech recognition init error")
        builder.setMessage("An error occurred when setting up speech recognition." +
                " The app will close now.")
        builder.create().show()
        exitProcess(0)
      } else {
        textToSpeech = TextToSpeech(this@CameraActivity,
                android.speech.tts.TextToSpeech.OnInitListener {
                  progressDialog.hide()
                  if (it != android.speech.tts.TextToSpeech.SUCCESS) {
                    val builder = AlertDialog.Builder(this@CameraActivity)
                    builder.setTitle("Text-to-speech init error")
                    builder.setMessage("An error occurred when setting up text-to-speech. " +
                            "The app will close now.")
                    builder.create().show()
                    exitProcess(0)
                  } else {
                    speechRecognition!!.start()
                  }
                })
      }
    }
  }

  inner class SpeechListener : SpeechRecognition.SpeechRecognitionListener {
    override fun onStart() {
      Toast.makeText(this@CameraActivity, "Start", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
      Toast.makeText(this@CameraActivity, "Pause", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
      Toast.makeText(this@CameraActivity, "Stop", Toast.LENGTH_SHORT).show()
    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_camera)
    if (null == savedInstanceState) {
      var exerciseString = intent.extras.getString("exercise")

      if (exerciseString.equals("front")) {
        exercise = FrontRaise()
      } else if (exerciseString.equals("curl")) {
        exercise = BicepsCurl()
      } else if (exerciseString.equals("lateral")) {
        exercise = LateralRaise()
      }

      fragmentManager
          .beginTransaction()
          .replace(R.id.container, Camera2BasicFragment.newInstance())
          .commit()
    }

    ResourceInitTask().execute()
  }

  override fun onResume() {
    super.onResume()
    if (!OpenCVLoader.initDebug()) {
      OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
    } else {
      mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
    }

    cameraFragment = fragmentManager.fragments[0] as Camera2BasicFragment
  }

  companion object {

    init {
      System.loadLibrary("opencv_java3")
    }

    @JvmStatic
    var isOpenCVInit = false
  }
}
