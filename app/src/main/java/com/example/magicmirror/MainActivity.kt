package com.example.magicmirror

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.OrientationEventListener
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.ByteBuffer


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity : AppCompatActivity() {

    private lateinit var mHandler: Handler
    private lateinit var mainHandler: Handler
    private lateinit var mCameraId: String
    private lateinit var mImageReader: ImageReader
    private lateinit var mCameraManager: CameraManager
    private lateinit var mCameraDevice: CameraDevice
    private lateinit var mPreviewBuilder: CaptureRequest.Builder
    private lateinit var mSession: CameraCaptureSession

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setOnClickListener {
            takePreview()
        }

        surfaceView.post {
            initCameraAndPreview()
        }

        mCameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager


        val mSurfaceHolder = surfaceView.holder
        mSurfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                initCameraAndPreview();
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })
    }

    fun initCameraAndPreview() {
        val handlerThread = HandlerThread("My First Camera2")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
        mainHandler = Handler(mainLooper) //用来处理ui线程的handler，即ui线程
        try {
            mCameraId = "" + CameraCharacteristics.LENS_FACING_FRONT
            mImageReader = ImageReader.newInstance(surfaceView.width, surfaceView.height, ImageFormat.JPEG,  /*maxImages*/7)
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler) //这里必须传入mainHandler，因为涉及到了Ui操作
            mCameraManager = this.getSystemService(CAMERA_SERVICE) as CameraManager
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PackageManager.PERMISSION_GRANTED)
                return
            }
            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler)
        } catch (e: CameraAccessException) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    private val mOnImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader -> //进行相片存储
            mCameraDevice.close()
            //mSurfaceView.setVisibility(View.GONE);//存疑
            val image: Image = reader.acquireNextImage()
            val buffer: ByteBuffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes) //将image对象转化为byte，再转化为bitmap
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                img_show.setImageBitmap(bitmap)
            }
        }

    private val deviceStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            try {
                takePreview()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onDisconnected(@NonNull camera: CameraDevice) {
            mCameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Toast.makeText(this@MainActivity, "打开摄像头失败", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(CameraAccessException::class)
    fun takePreview() {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mPreviewBuilder.addTarget(surfaceView.holder.surface)
        mCameraDevice.createCaptureSession(listOf(surfaceView.holder.surface, mImageReader.surface), mSessionPreviewStateCallback, mHandler)
    }


    private val mSessionPreviewStateCallback: CameraCaptureSession.StateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            mSession = session
            //配置完毕开始预览
            try {
                /**
                 * 设置你需要配置的参数
                 */
                //自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                //打开闪光灯
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                //无限次的重复获取图像
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Toast.makeText(this@MainActivity, "配置失败", Toast.LENGTH_SHORT).show()
        }
    }
    private val mSessionCaptureCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            mSession = session
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            mSession = session
        }

        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
            super.onCaptureFailed(session, request, failure)
        }
    }


    fun takePicture() {
        try {
            val captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE) //用来设置拍照请求的request
            captureRequestBuilder.addTarget(mImageReader.surface)
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            val rotation = windowManager.defaultDisplay.rotation
            val cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId)
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(cameraCharacteristics, rotation)) //使图片做顺时针旋转
            val mCaptureRequest = captureRequestBuilder.build()
            mSession.capture(mCaptureRequest, null, mHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    //获取图片应该旋转的角度，使图片竖直
    fun getOrientation(rotation: Int): Int {
        return when (rotation) {
            Surface.ROTATION_0 -> 90
            Surface.ROTATION_90 -> 0
            Surface.ROTATION_180 -> 270
            Surface.ROTATION_270 -> 180
            else -> 0
        }
    }

    //获取图片应该旋转的角度，使图片竖直
    private fun getJpegOrientation(c: CameraCharacteristics, deviceOrientation: Int): Int {
        var deviceOrientation = deviceOrientation
        if (deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) return 0
        val sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90

        // LENS_FACING相对于设备屏幕的方向,LENS_FACING_FRONT相机设备面向与设备屏幕相同的方向
        val facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        if (facingFront) deviceOrientation = -deviceOrientation

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + deviceOrientation + 360) % 360
    }


}