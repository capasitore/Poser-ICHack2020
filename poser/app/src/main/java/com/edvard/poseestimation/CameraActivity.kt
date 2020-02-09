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
import android.content.Intent
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
import java.util.*
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

    var textToSpeech: TextToSpeech? = null
    var exercise: Exercise? = null
    var exerciseDesc: String? = null
    var working = false

    private var progressDialog: ProgressDialog? = null

    inner class CountdownTask: TimerTask() {
        var time : Int = 11

        override fun run() {
            time -= 1
            if (time > 0) {
                textToSpeech!!.speak(time.toString())
            } else {
                textToSpeech!!.speak("Go!")
                working = true
                cancel()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        progressDialog = ProgressDialog(this@CameraActivity)

        progressDialog!!.setMessage("Loading, please wait...")
        progressDialog!!.show()

        textToSpeech = TextToSpeech(this@CameraActivity,
                android.speech.tts.TextToSpeech.OnInitListener {
                    progressDialog!!.hide()
                    if (it != android.speech.tts.TextToSpeech.SUCCESS) {
                        val builder = AlertDialog.Builder(this@CameraActivity)
                        builder.setTitle("Text-to-speech init error")
                        builder.setMessage("An error occurred when setting up text-to-speech. " +
                                "The app will close now.")
                        builder.create().show()
                        exitProcess(0)
                    } else {
                        if (null == savedInstanceState) {
                            var exerciseString = intent.extras.getString("exercise")

                            if (exerciseString.equals("front")) {
                                exercise = FrontRaise()
                                exerciseDesc = "Front raise"
                            } else if (exerciseString.equals("curl")) {
                                exercise = BicepsCurl()
                                exerciseDesc = "Biceps curl"
                            } else if (exerciseString.equals("lateral")) {
                                exercise = LateralRaise()
                                exerciseDesc = "Lateral raise"
                            }

                            fragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                                    .commit()

                            val timer = Timer()
                            textToSpeech!!.speak("$exerciseDesc starting in ", Runnable {
                                timer.schedule(CountdownTask(), 250, 1000)
                            })
                        }
                    }
                })
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

  private fun startSummaryPage(){
    val intent = Intent(this, CameraActivity::class.java)
    val errors = exercise!!.errors.toList().sortedBy { (k, v) -> v }

    intent.putExtra("num_errors", errors.size)
    errors.forEachIndexed { i, x ->
      intent.putExtra("error$i", x.toString())
    }

    intent.putExtra("reps", exercise!!.reps)
    startActivity(intent)
  }
}
