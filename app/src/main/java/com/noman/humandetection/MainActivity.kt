package com.noman.humandetection

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.noman.humandetection.databinding.ActivityMainBinding

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
    }

    private fun requestCameraAndStartScanner(){
        if (isPermissionGranted(cameraPermission)){
            startScanner()
        }else {
            requestCameraPermission()
        }
    }

    private fun startScanner(){
        ScannerActivity.startScanner(this){ barcodes ->
            barcodes.forEach{barcode ->
                when(barcode.valueType){
                    Barcode.TYPE_URL ->{
                        binding.txtQrCodeInfo.text = barcode.url.toString()
                    }else ->{
                        binding.txtQrCodeInfo.text = barcode.rawValue.toString()
                    }
                }
            }
        }
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