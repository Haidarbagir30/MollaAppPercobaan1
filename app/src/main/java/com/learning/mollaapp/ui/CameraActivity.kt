package com.learning.mollaapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.learning.mollaapp.R
import com.learning.mollaapp.cameraApi.ApiClientCamera
import com.learning.mollaapp.cameraApi.ApiServiceCamera
import com.learning.mollaapp.cameraApi.ImageResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var apiServiceCamera: ApiServiceCamera
    private lateinit var cameraView: PreviewView
    private lateinit var tvResult: TextView
    private lateinit var btTranslate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Inisialisasi komponen UI
        tvResult = findViewById(R.id.tv_result)
        btTranslate = findViewById(R.id.btTranslate)
        cameraView = findViewById(R.id.cameraView)

        // Inisialisasi Retrofit
        apiServiceCamera = ApiClientCamera.apiServiceCamera

        // Inisialisasi executor untuk pemrosesan gambar dari kamera
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Memeriksa izin kamera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Menghubungkan tombol translate dengan metode terjemahan
        btTranslate.setOnClickListener {
            // Panggil metode untuk menerjemahkan gambar atau lakukan aksi yang diinginkan
            processImage()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        processImage()
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage() {
        // Dalam metode ini, kita tidak perlu ImageProxy karena kita hanya mengirim permintaan ke API untuk membaca gambar
        // Jadi, kita cukup mengirim permintaan ke API dan menanggapi responsnya

        // Kirim data gambar ke API untuk membaca gambar
        apiServiceCamera.readImage(createImagePart()).enqueue(object : Callback<ImageResponse> {
            override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                if (response.isSuccessful) {
                    val imageResponse = response.body()
                    imageResponse?.let {
                        // Proses respons pembacaan gambar di sini
                        Log.d(TAG, "Read Image Response: $it")
                        tvResult.text = it.translatedText ?: "No translation found"
                    }
                } else {
                    Log.e(TAG, "Error reading image: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                Log.e(TAG, "Error reading image", t)
            }
        })
    }

    private fun createImagePart(): MultipartBody.Part {
        val imageRequestBody = ByteArray(0).toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", "image.jpg", imageRequestBody)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
