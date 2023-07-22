package com.noman.humandetection.qr_scanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.noman.humandetection.CameraXViewModel
import com.noman.humandetection.databinding.ActivityScannerBinding
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding

    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val cameraXViewModel = viewModels<CameraXViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

       cameraXViewModel.value.processCameraProvider.observe(this){ provider ->
           processCameraProvider = provider
           bindCameraPreview()
           bindInputAnalyser()
       }
    }

    private fun bindInputAnalyser() {
        val barcodeScanner:BarcodeScanner = BarcodeScanning.getClient(
           BarcodeScannerOptions.Builder()
               .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
               .build()
        )
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor){ imageProxy ->
            processImageProxy(barcodeScanner,imageProxy)
        }

        processCameraProvider.bindToLifecycle(this, cameraSelector , imageAnalysis)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy)
    {
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()){
                    showBarcodeInfo(barcodes.first())
                    finish()
                }
            }.addOnFailureListener{
                it.printStackTrace()
                Log.e("nlog-error",it.toString())
            }.addOnCompleteListener{
                imageProxy.close()
            }
    }

    private fun showBarcodeInfo(barcode: Barcode) {

            when(barcode.valueType){
                Barcode.TYPE_URL ->{
                   // binding.txtQrCodeInfo.text = barcode.url.toString()
                    Toast.makeText(this, barcode.url.toString(),Toast.LENGTH_SHORT).show()
                }else ->{
                //binding.txtQrCodeInfo.text = barcode.rawValue.toString()
                Toast.makeText(this, barcode.rawValue.toString(),Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun bindCameraPreview(){
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
        processCameraProvider.bindToLifecycle(this, cameraSelector , cameraPreview)
    }
    companion object{
        private var TAG = ScannerActivity::class.simpleName
        fun startScanner(context: Context){
            Intent(context, ScannerActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }

}