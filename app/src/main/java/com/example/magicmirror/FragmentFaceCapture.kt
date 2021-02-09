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
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.fragment_face_capture.*
import java.nio.ByteBuffer

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class FragmentFaceCapture : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_face_capture, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        face_capture_bt_capture.setOnClickListener {
            initCameraAndPreview()
            face_capture_surfaceView.postDelayed({
                takePicture()
                Toast.makeText(context, "已获得图片bitmap，待上传", Toast.LENGTH_SHORT).show()
            }, 500)
        }

        face_capture_surfaceView.post {
            initCameraAndPreview()
        }

        mCameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager


        val mSurfaceHolder = face_capture_surfaceView.holder
        mSurfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                initCameraAndPreview()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })

    }













    // --------
    private lateinit var mHandler: Handler
    private lateinit var mainHandler: Handler
    private lateinit var mCameraId: String
    private lateinit var mImageReader: ImageReader
    private lateinit var mCameraManager: CameraManager
    private lateinit var mCameraDevice: CameraDevice
    private lateinit var mPreviewBuilder: CaptureRequest.Builder
    private lateinit var mSession: CameraCaptureSession


    fun initCameraAndPreview() {
        val handlerThread = HandlerThread("My First Camera2")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
        mainHandler = Handler(activity!!.mainLooper) //用来处理ui线程的handler，即ui线程
        try {
            mCameraId = "" + CameraCharacteristics.LENS_FACING_BACK
            mImageReader = ImageReader.newInstance(face_capture_surfaceView.width, face_capture_surfaceView.height, ImageFormat.JPEG,  /*maxImages*/7)
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler) //这里必须传入mainHandler，因为涉及到了Ui操作
            mCameraManager = activity!!.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
            if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), PackageManager.PERMISSION_GRANTED)
                return
            }
            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler)
        } catch (e: CameraAccessException) {
            Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
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
                face_capture_img_show.setImageBitmap(bitmap)
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
            mCameraDevice.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Toast.makeText(activity!!, "打开摄像头失败", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(CameraAccessException::class)
    fun takePreview() {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mPreviewBuilder.addTarget(face_capture_surfaceView.holder.surface)
        mCameraDevice.createCaptureSession(listOf(face_capture_surfaceView.holder.surface, mImageReader.surface), mSessionPreviewStateCallback, mHandler)
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
            Toast.makeText(activity!!, "配置失败", Toast.LENGTH_SHORT).show()
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


    private fun takePicture() {
        try {
            val captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE) //用来设置拍照请求的request
            captureRequestBuilder.addTarget(mImageReader.surface)
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            val rotation = activity!!.windowManager.defaultDisplay.rotation
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
        var deviceOrientationTmp = deviceOrientation
        if (deviceOrientationTmp == OrientationEventListener.ORIENTATION_UNKNOWN) return 0
        val sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        // Round device orientation to a multiple of 90
        deviceOrientationTmp = (deviceOrientationTmp + 45) / 90 * 90

        // LENS_FACING相对于设备屏幕的方向,LENS_FACING_FRONT相机设备面向与设备屏幕相同的方向
        val facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        if (facingFront) deviceOrientationTmp = -deviceOrientationTmp

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + deviceOrientationTmp + 360) % 360
    }
}