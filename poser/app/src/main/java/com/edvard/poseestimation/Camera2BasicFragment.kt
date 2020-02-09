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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v13.app.FragmentCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_camera2_basic.*
import org.ichack20.poser.Pose
import org.ichack20.poser.PoseEstimator
import org.ichack20.poser.exercises.Exercise
import org.ichack20.poser.exercises.LateralRaise
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Basic fragments for the Camera.
 */
class Camera2BasicFragment : Fragment(), FragmentCompat.OnRequestPermissionsResultCallback {

  private val lock = Any()
  private var runClassifier = false
  private var checkedPermissions = false
  private var textureView: AutoFitTextureView? = null
  private var debugger: TextView? = null
  private var mp: MediaPlayer? = null
  private var layoutFrame: AutoFitFrameLayout? = null
  private var bro_toggle: Switch? = null
//  private var drawView: DrawView? = null
  private var classifier: ImageClassifier? = null


  private var reps_counter: TextView? = null
  /**
   * [TextureView.SurfaceTextureListener] handles several lifecycle events on a [ ].
   */
  private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

    override fun onSurfaceTextureAvailable(
      texture: SurfaceTexture,
      width: Int,
      height: Int
    ) {
      openCamera(width, height)
    }

    override fun onSurfaceTextureSizeChanged(
      texture: SurfaceTexture,
      width: Int,
      height: Int
    ) {
      configureTransform(width, height)
    }

    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
      return true
    }

    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
  }

  /**
   * ID of the current [CameraDevice].
   */
  private var cameraId: String? = null

  /**
   * A [CameraCaptureSession] for camera preview.
   */
  private var captureSession: CameraCaptureSession? = null

  /**
   * A reference to the opened [CameraDevice].
   */
  private var cameraDevice: CameraDevice? = null

  /**
   * The [android.util.Size] of camera preview.
   */
  private var previewSize: Size? = null

  /**
   * [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.
   */
  private val stateCallback = object : CameraDevice.StateCallback() {

    override fun onOpened(currentCameraDevice: CameraDevice) {
      // This method is called when the camera is opened.  We start camera preview here.
      cameraOpenCloseLock.release()
      cameraDevice = currentCameraDevice
      createCameraPreviewSession()
    }

    override fun onDisconnected(currentCameraDevice: CameraDevice) {
      cameraOpenCloseLock.release()
      currentCameraDevice.close()
      cameraDevice = null
    }

    override fun onError(
      currentCameraDevice: CameraDevice,
      error: Int
    ) {
      cameraOpenCloseLock.release()
      currentCameraDevice.close()
      cameraDevice = null
      val activity = activity
      activity?.finish()
    }
  }

  /**
   * An additional thread for running tasks that shouldn't block the UI.
   */
  private var backgroundThread: HandlerThread? = null

  /**
   * A [Handler] for running tasks in the background.
   */
  private var backgroundHandler: Handler? = null

  /**
   * An [ImageReader] that handles image capture.
   */
  private var imageReader: ImageReader? = null

  /**
   * [CaptureRequest.Builder] for the camera preview
   */
  private var previewRequestBuilder: CaptureRequest.Builder? = null

  /**
   * [CaptureRequest] generated by [.previewRequestBuilder]
   */
  private var previewRequest: CaptureRequest? = null

  /**
   * A [Semaphore] to prevent the app from exiting before closing the camera.
   */
  private val cameraOpenCloseLock = Semaphore(1)

  /**
   * A [CameraCaptureSession.CaptureCallback] that handles events related to capture.
   */
  private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

    override fun onCaptureProgressed(
      session: CameraCaptureSession,
      request: CaptureRequest,
      partialResult: CaptureResult
    ) {
    }

    override fun onCaptureCompleted(
      session: CameraCaptureSession,
      request: CaptureRequest,
      result: TotalCaptureResult
    ) {
    }
  }

  private val requiredPermissions: Array<String>
    get() {
      val activity = activity
      return try {
        val info = activity
            .packageManager
            .getPackageInfo(activity.packageName, PackageManager.GET_PERMISSIONS)
        val ps = info.requestedPermissions
        if (ps != null && ps.isNotEmpty()) {
          ps
        } else {
          arrayOf()
        }
      } catch (e: Exception) {
        arrayOf()
      }

    }

  /**
   * Takes photos and classify them periodically.
   */
  private val periodicClassify = object : Runnable {
    override fun run() {
      synchronized(lock) {
        if (runClassifier) {
          classifyFrame()
        }
      }
      backgroundHandler!!.post(this)
    }
  }

  /**
   * Shows a [Toast] on the UI thread for the classification results.
   *
   * @param text The message to show
   */
  private fun showToast(text: String) {
    val activity = activity
    activity?.runOnUiThread {
//      drawView!!.invalidate()
    }
  }

  private var exercise : Exercise? = null

  /**
   * Layout the preview and buttons.
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    return inflater.inflate(R.layout.fragment_camera2_basic, container, false)
  }

  /**
   * Connect the buttons to their event handler.
   */
  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    textureView = view.findViewById(R.id.texture)
    layoutFrame = view.findViewById(R.id.layout_frame)
//    drawView = view.findViewById(R.id.drawview)
    debugger = view.findViewById(R.id.angle_debug)
    bro_toggle = view.findViewById(R.id.bro_switch)
    mp = MediaPlayer.create(activity, R.raw.bro1)

//    val displayMetrics = Resources.getSystem().displayMetrics
//
//    drawView!!.setImgSize( displayMetrics.heightPixels,  displayMetrics.widthPixels)

    reps_counter = view.findViewById(R.id.rep_counter_txt)

    exercise = (activity as CameraActivity).exercise
  }

  /**
   * Load the model and labels.
   */
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    try {
      // create either a new ImageClassifierQuantizedMobileNet or an ImageClassifierFloatInception
      //      classifier = new ImageClassifierQuantizedMobileNet(getActivity());
      classifier = ImageClassifierFloatInception.create(activity)
//      if (drawView != null){
//        val displayMetrics = Resources.getSystem().getDisplayMetrics()
//        println("DISPLAY  h: ${displayMetrics.heightPixels}, w: ${displayMetrics.widthPixels}")
//        drawView!!.setImgSize( displayMetrics.heightPixels,  displayMetrics.widthPixels)
//      }
    } catch (e: IOException) {
      Log.e(TAG, "Failed to initialize an image classifier.", e)
    }
  }

  @Synchronized
  override fun onResume() {
    super.onResume()

    backgroundThread = HandlerThread(HANDLE_THREAD_NAME)
    backgroundThread!!.start()
    backgroundHandler = Handler(backgroundThread!!.getLooper())
    runClassifier = true

    startBackgroundThread(Runnable { classifier!!.initTflite(true) })
    startBackgroundThread(periodicClassify)

    // When the screen is turned off and turned back on, the SurfaceTexture is already
    // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
    // a camera and start preview from here (otherwise, we wait until the surface is ready in
    // the SurfaceTextureListener).
    if (textureView!!.isAvailable) {
      openCamera(textureView!!.width, textureView!!.height)
    } else {
      textureView!!.surfaceTextureListener = surfaceTextureListener
    }
  }

  override fun onPause() {
    closeCamera()
    stopBackgroundThread()
    super.onPause()
  }

  override fun onDestroy() {
    classifier!!.close()
    super.onDestroy()
  }

  /**
   * Sets up member variables related to camera.
   *
   * @param width  The width of available size for camera preview
   * @param height The height of available size for camera preview
   */
  private fun setUpCameraOutputs(
    width: Int,
    height: Int
  ) {
    val activity = activity
    val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    try {
      for (cameraId in manager.cameraIdList) {
        val characteristics = manager.getCameraCharacteristics(cameraId)

        // We don't use a front facing camera in this sample.
        val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue
        }

        val map =
          characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue

        // // For still image captures, we use the largest available size.
        val largest = Collections.max(
            Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea()
        )
        imageReader = ImageReader.newInstance(
            largest.width, largest.height, ImageFormat.JPEG, /*maxImages*/ 2
        )

        // Find out if we need to swap dimension to get the preview size relative to sensor
        // coordinate.
        val displayRotation = activity.windowManager.defaultDisplay.rotation

        /* Orientation of the camera sensor */
        val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        var swappedDimensions = false
        when (displayRotation) {
          Surface.ROTATION_0, Surface.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) {
            swappedDimensions = true
          }
          Surface.ROTATION_90, Surface.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) {
            swappedDimensions = true
          }
          else -> Log.e(TAG, "Display rotation is invalid: $displayRotation")
        }

        val displaySize = Point()
        activity.windowManager.defaultDisplay.getSize(displaySize)
        var rotatedPreviewWidth = width
        var rotatedPreviewHeight = height
        var maxPreviewWidth = displaySize.x
        var maxPreviewHeight = displaySize.y

        if (swappedDimensions) {
          rotatedPreviewWidth = height
          rotatedPreviewHeight = width
          maxPreviewWidth = displaySize.y
          maxPreviewHeight = displaySize.x
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
          maxPreviewWidth = MAX_PREVIEW_WIDTH
        }

        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
          maxPreviewHeight = MAX_PREVIEW_HEIGHT
        }

        val displayMetrics = Resources.getSystem().getDisplayMetrics()
        previewSize = Size(displayMetrics.heightPixels, displayMetrics.widthPixels)

        // We fit the aspect ratio of TextureView to the size of preview we picked.
//        val orientation = resources.configuration.orientation
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//          layoutFrame!!.setAspectRatio(previewSize!!.width, previewSize!!.height)
//          textureView!!.setAspectRatio(previewSize!!.width, previewSize!!.height)
//          drawView!!.setAspectRatio(previewSize!!.width, previewSize!!.height)
//        } else {
        layoutFrame!!.setAspectRatio(previewSize!!.height, previewSize!!.width)
        textureView!!.setAspectRatio(previewSize!!.height, previewSize!!.width)
//        drawView!!.setAspectRatio(previewSize!!.height, previewSize!!.width)
//        }

        this.cameraId = cameraId
        return
      }
    } catch (e: CameraAccessException) {
      Log.e(TAG, "Failed to access Camera", e)
    } catch (e: NullPointerException) {
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      ErrorDialog.newInstance(getString(R.string.camera_error))
          .show(childFragmentManager, FRAGMENT_DIALOG)
    }

  }

  /**
   * Opens the camera specified by [Camera2BasicFragment.cameraId].
   */
  @SuppressLint("MissingPermission")
  private fun openCamera(
    width: Int,
    height: Int
  ) {
    if (!checkedPermissions && !allPermissionsGranted()) {
      FragmentCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE)
      return
    } else {
      checkedPermissions = true
    }
    setUpCameraOutputs(width, height)
    configureTransform(width, height)
    val activity = activity
    val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    try {
      if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw RuntimeException("Time out waiting to lock camera opening.")
      }
      manager.openCamera(cameraId!!, stateCallback, backgroundHandler)
    } catch (e: CameraAccessException) {
      Log.e(TAG, "Failed to open Camera", e)
    } catch (e: InterruptedException) {
      throw RuntimeException("Interrupted while trying to lock camera opening.", e)
    }

  }

  private fun allPermissionsGranted(): Boolean {
    for (permission in requiredPermissions) {
      if (ContextCompat.checkSelfPermission(
              activity, permission
          ) != PackageManager.PERMISSION_GRANTED
      ) {
        return false
      }
    }
    return true
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  /**
   * Closes the current [CameraDevice].
   */
  private fun closeCamera() {
    try {
      cameraOpenCloseLock.acquire()
      if (null != captureSession) {
        captureSession!!.close()
        captureSession = null
      }
      if (null != cameraDevice) {
        cameraDevice!!.close()
        cameraDevice = null
      }
      if (null != imageReader) {
        imageReader!!.close()
        imageReader = null
      }
    } catch (e: InterruptedException) {
      throw RuntimeException("Interrupted while trying to lock camera closing.", e)
    } finally {
      cameraOpenCloseLock.release()
    }
  }

  /**
   * Starts a background thread and its [Handler].
   */
  @Synchronized
  protected fun startBackgroundThread(r: Runnable) {
    if (backgroundHandler != null) {
      backgroundHandler!!.post(r)
    }
  }

  /**
   * Stops the background thread and its [Handler].
   */
  private fun stopBackgroundThread() {
    backgroundThread!!.quitSafely()
    try {
      backgroundThread!!.join()
      backgroundThread = null
      backgroundHandler = null
      synchronized(lock) {
        runClassifier = false
      }
    } catch (e: InterruptedException) {
      Log.e(TAG, "Interrupted when stopping background thread", e)
    }

  }

  /**
   * Creates a new [CameraCaptureSession] for camera preview.
   */
  private fun createCameraPreviewSession() {
    try {
      val texture = textureView!!.surfaceTexture!!

      // We configure the size of default buffer to be the size of camera preview we want.
      texture.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)

      // This is the output Surface we need to start preview.
      val surface = Surface(texture)

      // We set up a CaptureRequest.Builder with the output Surface.
      previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
      previewRequestBuilder!!.addTarget(surface)

      // Here, we create a CameraCaptureSession for camera preview.
      cameraDevice!!.createCaptureSession(
          Arrays.asList(surface),
          object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
              // The camera is already closed
              if (null == cameraDevice) {
                return
              }

              // When the session is ready, we start displaying the preview.
              captureSession = cameraCaptureSession
              try {
                // Auto focus should be continuous for camera preview.
                previewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )

                // Finally, we start displaying the camera preview.
                previewRequest = previewRequestBuilder!!.build()
                captureSession!!.setRepeatingRequest(
                    previewRequest!!, captureCallback, backgroundHandler
                )
              } catch (e: CameraAccessException) {
                Log.e(TAG, "Failed to set up config to capture Camera", e)
              }

            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
              showToast("Failed")
            }
          }, null
      )
    } catch (e: CameraAccessException) {
      Log.e(TAG, "Failed to preview Camera", e)
    }

  }

  /**
   * Configures the necessary [android.graphics.Matrix] transformation to `textureView`. This
   * method should be called after the camera preview size is determined in setUpCameraOutputs and
   * also the size of `textureView` is fixed.
   *
   * @param viewWidth  The width of `textureView`
   * @param viewHeight The height of `textureView`
   */
  private fun configureTransform(
    viewWidth: Int,
    viewHeight: Int
  ) {
    val activity = activity
    if (null == textureView || null == previewSize || null == activity) {
      return
    }
    val rotation = activity.windowManager.defaultDisplay.rotation
    val matrix = Matrix()
    val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
    val bufferRect = RectF(0f, 0f, previewSize!!.height.toFloat(), previewSize!!.width.toFloat())
    val centerX = viewRect.centerX()
    val centerY = viewRect.centerY()
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
      val scale = Math.max(
          viewHeight.toFloat() / previewSize!!.height,
          viewWidth.toFloat() / previewSize!!.width
      )
      matrix.postScale(scale, scale, centerX, centerY)
      matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
    } else if (Surface.ROTATION_180 == rotation) {
      matrix.postRotate(180f, centerX, centerY)
    }
    textureView!!.setTransform(matrix)
  }


  val bro_sounds = intArrayOf(R.raw.bro1,R.raw.bro2,R.raw.bro3)
  var old_rep_counter = 0

  /**
   * Classifies a frame from the preview stream.
   */
  @SuppressLint("SetTextI18n")
  private fun classifyFrame() {
    if (classifier == null || activity == null || cameraDevice == null) {
      showToast("Uninitialized Classifier or invalid context.")
      return
    }

    val bitmap = textureView!!.getBitmap(classifier!!.imageSizeX, classifier!!.imageSizeY)

    val ps = PoseEstimator(classifier)
    val pose = ps.processFrame(bitmap)

    if ((activity as CameraActivity).working) {
      exercise!!.update(pose, (activity as CameraActivity).textToSpeech)
    }


//    mp.setVolume(1f, 1f)

    if (old_rep_counter != exercise!!.reps){
      old_rep_counter = exercise!!.reps
      activity.runOnUiThread{reps_counter!!.text = old_rep_counter.toString()}


      if (old_rep_counter % 3 == 1 && bro_toggle!!.isChecked){
        var bro_id = Random.nextInt(bro_sounds.size)
        mp!!.reset()
        var afd = context.getResources().openRawResourceFd(bro_sounds[bro_id])

        mp!!.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength())
        mp!!.prepare()

        mp!!.start()
//        mp.release()
      }

    }

    var txt = "ls: ${pose.getAngle(Pose.Angle.L_SHOULDER).toInt()}" +
            " rs: ${pose.getAngle(Pose.Angle.R_SHOULDER).toInt()}" +
            " le: ${pose.getAngle(Pose.Angle.L_ELBOW).toInt()}" +
            " re: ${pose.getAngle(Pose.Angle.R_ELBOW).toInt()}" +
            " rh: ${pose.getAngle(Pose.Angle.R_HIP).toInt()}"

    activity.runOnUiThread{debugger!!.text = txt}


//    val textToShow = classifier!!.classifyFrame(bitmap)
    bitmap.recycle()

//    drawView!!.setDrawPoint(pose.rawPoints, 0.5f)
//    showToast(textToShow)
  }

  /**
   * Compares two `Size`s based on their areas.
   */
  private class CompareSizesByArea : Comparator<Size> {

    override fun compare(
      lhs: Size,
      rhs: Size
    ): Int {
      // We cast here to ensure the multiplications won't overflow
      return java.lang.Long.signum(
          lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height
      )
    }
  }

  /**
   * Shows an error message dialog.
   */
  class ErrorDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
      val activity = activity
      return AlertDialog.Builder(activity)
          .setMessage(arguments.getString(ARG_MESSAGE))
          .setPositiveButton(
              android.R.string.ok
          ) { dialogInterface, i -> activity.finish() }
          .create()
    }

    companion object {

      private val ARG_MESSAGE = "message"

      fun newInstance(message: String): ErrorDialog {
        val dialog = ErrorDialog()
        val args = Bundle()
        args.putString(ARG_MESSAGE, message)
        dialog.arguments = args
        return dialog
      }
    }
  }

  companion object {

    /**
     * Tag for the [Log].
     */
    private const val TAG = "TfLiteCameraDemo"

    private const val FRAGMENT_DIALOG = "dialog"

    private const val HANDLE_THREAD_NAME = "CameraBackground"

    private const val PERMISSIONS_REQUEST_CODE = 1

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private const val MAX_PREVIEW_WIDTH = 1080

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private const val MAX_PREVIEW_HEIGHT = 2340

    /**
     * Resizes image.
     *
     *
     * Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation,
     * resulting in gorgeous previews but the storage of garbage capture data.
     *
     *
     * Given `choices` of `Size`s supported by a camera, choose the smallest one that is
     * at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size, and
     * whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal `Size`, or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(
      choices: Array<Size>,
      textureViewWidth: Int,
      textureViewHeight: Int,
      maxWidth: Int,
      maxHeight: Int,
      aspectRatio: Size
    ): Size {
      // Collect the supported resolutions that are at least as big as the preview Surface
      val bigEnough = ArrayList<Size>()
      // Collect the supported resolutions that are smaller than the preview Surface
      val notBigEnough = ArrayList<Size>()
      val w = aspectRatio.width
      val h = aspectRatio.height
      for (option in choices) {
        if (option.width <= maxWidth
            && option.height <= maxHeight
            && option.height == option.width * h / w
        ) {
          if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
            bigEnough.add(option)
          } else {
            notBigEnough.add(option)
          }
        }
      }

      // Pick the smallest of those big enough. If there is no one big enough, pick the
      // largest of those not big enough.
      return when {
        bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
        notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
        else -> {
          Log.e(TAG, "Couldn't find any suitable preview size")
          choices[0]
        }
      }
    }

    fun newInstance(): Camera2BasicFragment {
      return Camera2BasicFragment()
    }
  }
}
