package com.learning.mollaapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.learning.mollaapp.R
import com.learning.mollaapp.cameraApi.ApiClientCamera
import com.learning.mollaapp.cameraApi.ApiServiceCamera
import com.learning.mollaapp.cameraApi.ReadImageResponse
import com.learning.mollaapp.cameraApi.TranslationResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var apiServiceCamera: ApiServiceCamera
    private lateinit var cameraView:SurfaceView
    private lateinit var tvResult: TextView
    private lateinit var btTranslate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Inisialisasi komponen UI
        tvResult = findViewById(R.id.tv_result)
        btTranslate = findViewById(R.id.btTranslate)

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
                        processImage(imageProxy)
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


    @OptIn(ExperimentalGetImage::class) private fun processImage(imageProxy: ImageProxy) {
        val image = imageProxy.image
        val buffer: ByteBuffer = image?.planes?.get(0)?.buffer ?: return
        val data = ByteArray(buffer.remaining())

        buffer.get(data)

        // Kirim data gambar ke API untuk membaca gambar
        val imageRequestBody = data.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", imageRequestBody)

        apiServiceCamera.readImage(imagePart).enqueue(object : Callback<ReadImageResponse> {
            override fun onResponse(
                call: Call<ReadImageResponse>,
                response: Response<ReadImageResponse>
            ) {
                val readImageResponse = response.body()
                readImageResponse?.let {
                    // Proses respons pembacaan gambar di sini
                    Log.d(TAG, "Read Image Response: $it")
                    tvResult.text = it.imageUrl ?: "No sentence found"
                }
                imageProxy.close()
            }

            override fun onFailure(call: Call<ReadImageResponse>, t: Throwable) {
                Log.e(TAG, "Error reading image", t)
                imageProxy.close()
            }
        })
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
