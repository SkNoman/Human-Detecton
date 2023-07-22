package com.noman.humandetection

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.noman.humandetection.databinding.ActivityMainBinding
import com.noman.humandetection.face_detection.FaceDetectionActivity
import com.noman.humandetection.qr_scanner.ScannerActivity

class MainActivity : AppCompatActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if (isGranted){
          startScanner()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenScanner.setOnClickListener{
            requestCameraAndStartScanner()
        }
        binding.btnFaceDetection.setOnClickListener{
            FaceDetectionActivity.start(this)
        }
    }

    private fun requestCameraAndStartScanner(){
        if (isPermissionGranted(cameraPermission)){
            startScanner()
        }else {
            requestCameraPermission()
        }
    }

    private fun startScanner(){
        ScannerActivity.startScanner(this)
    }

    private fun requestCameraPermission() {
        when{
            shouldShowRequestPermissionRationale(cameraPermission) ->{
                cameraPermissionRequest {
                    openPermissionSetting()
                }
            }else ->{
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }


}