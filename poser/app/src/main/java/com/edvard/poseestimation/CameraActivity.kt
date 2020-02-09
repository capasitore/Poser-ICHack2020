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
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import org.ichack20.poser.SpeechRecognition
import org.ichack20.poser.TextToSpeech

import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

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
  private var textToSpeech: TextToSpeech? = null

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
        TODO("implement message that loading speech recognition failed")
      } else {
        textToSpeech = TextToSpeech(this@CameraActivity,
                android.speech.tts.TextToSpeech.OnInitListener {
                  progressDialog.hide()
                  if (it != android.speech.tts.TextToSpeech.SUCCESS) {
                    TODO("implement message that loading text-to-speech failed")
                  } else {
                    speechRecognition!!.start()
                  }
                })
      }
    }
  }

  inner class SpeechListener : SpeechRecognition.SpeechRecognitionListener {
    override fun onStart() {
      TODO("not implemented")
    }

    override fun onPause() {
      TODO("not implemented")
    }

    override fun onStop() {
      TODO("not implemented")
    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_camera)
    if (null == savedInstanceState) {
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
  }

  companion object {

    init {
      System.loadLibrary("opencv_java3")
    }

    @JvmStatic
    var isOpenCVInit = false
  }
}
