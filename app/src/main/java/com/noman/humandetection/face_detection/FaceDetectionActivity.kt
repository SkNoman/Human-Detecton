package com.noman.humandetection.face_detection

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.noman.humandetection.CameraXViewModel
import com.noman.humandetection.R
import com.noman.humandetection.databinding.ActivityFaceDetectionBinding
import com.noman.humandetection.qr_scanner.ScannerActivity
import java.util.concurrent.Executors

class FaceDetectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaceDetectionBinding

    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val cameraXViewModel = viewModels<CameraXViewModel>()

    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    private val detector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build()
        )
    }
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        binding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }

        cameraXViewModel.value.processCameraProvider.observe(this){ provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyser()
        }
    }

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
            CameraSelector.LENS_FACING_BACK
        else
            CameraSelector.LENS_FACING_FRONT

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        binding.faceBoxOverlay.clear()
        processCameraProvider.unbindAll()
        bindCameraPreview()
        bindInputAnalyser()
    }

    private fun bindInputAnalyser() {
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.faceDetectionPreview.display.rotation)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor){ imageProxy ->
            processImageProxy(detector,imageProxy)
        }

        processCameraProvider.bindToLifecycle(this, cameraSelector , imageAnalysis)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(detector: FaceDetector, imageProxy: ImageProxy)
    {
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                binding.faceBoxOverlay.clear()
                faces.forEach{face ->
                    val box = FaceBox(binding.faceBoxOverlay,face,imageProxy.cropRect)
                    binding.faceBoxOverlay.add(box)
                }
            }.addOnFailureListener{
                it.printStackTrace()
                Log.e("nlog-error",it.toString())
            }.addOnCompleteListener{
                imageProxy.close()
            }
    }


    private fun bindCameraPreview(){
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.faceDetectionPreview.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.faceDetectionPreview.surfaceProvider)
        processCameraProvider.bindToLifecycle(this, cameraSelector , cameraPreview)
    }

    companion object{
      //  private var TAG = FaceDetectionActivity::class.simpleName
        fun start(context: Context){
            Intent(context, FaceDetectionActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}