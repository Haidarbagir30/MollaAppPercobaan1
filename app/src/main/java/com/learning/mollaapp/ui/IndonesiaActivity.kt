package com.learning.mollaapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.learning.mollaapp.R
import com.learning.mollaapp.database.ApiClient
import com.learning.mollaapp.database.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response as RetrofitResponse // Alias untuk membedakan dengan kelas Response milik Anda

class IndonesiaActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var originalSentenceTextView: TextView
    private lateinit var shuffledSentenceEditText: EditText
    private lateinit var submitButton: Button

    private var responseData: Response? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indonesia)

        // Inisialisasi UI
        resultTextView = findViewById(R.id.correct_translation)
        originalSentenceTextView = findViewById(R.id.original_sentence)
        shuffledSentenceEditText = findViewById(R.id.shuffled_sentence)
        submitButton = findViewById(R.id.submit_button)

        // Panggil fungsi untuk mendapatkan data dari API
        getQuestions()

        // Atur listener untuk tombol submit
        submitButton.setOnClickListener {
            // Handle logika pemrosesan jawaban pengguna di sini
            // Misalnya, bandingkan jawaban pengguna dengan jawaban yang benar
            // dan berikan feedback ke pengguna
            checkUserAnswer()
        }
    }

    private fun getQuestions() {
        val call: Call<Response> = ApiClient.apiService.getQuestions()

        call.enqueue(object : Callback<Response> {
            override fun onResponse(
                call: Call<Response>,
                response: RetrofitResponse<Response>
            ) {
                if (response.isSuccessful) {
                    // Tangani hasil respons di sini
                    responseData = response.body()
                    // Tampilkan atau proses data sesuai kebutuhan Anda
                    val resultText = responseData?.sentence?.correctTranslation ?: "Data API tidak valid."
                    resultTextView.text = resultText

                    // Tampilkan original sentence
                    originalSentenceTextView.text = responseData?.sentence?.originalSentence ?: "Data API tidak valid."
                    resultTextView.text = resultText

                    // Set nilai default untuk shuffled sentence di EditText
                    shuffledSentenceEditText.setText(responseData?.sentence?.shuffledSentence)
                } else {
                    // Tangani kesalahan jika respons tidak berhasil
                    resultTextView.text = getString(R.string.api_error)
                }
            }

            override fun onFailure(call: Call<Response>, t: Throwable) {
                // Tangani kesalahan jaringan atau kesalahan lainnya
                resultTextView.text = getString(R.string.error_message, t.message)
            }
        })
    }

    private fun checkUserAnswer() {
        // Dapatkan jawaban pengguna dari EditText
        val userAnswer = shuffledSentenceEditText.text.toString()

        // Bandingkan jawaban pengguna dengan jawaban yang benar
        val correctAnswer = responseData?.sentence?.correctTranslation ?: ""

        // Bandingkan jawaban pengguna dengan jawaban yang benar (ignoring case)
        val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)

        // Berikan feedback ke pengguna berdasarkan hasil perbandingan
        if (isCorrect) {
            showToast("Jawaban benar!")
        } else {
            showToast("Jawaban salah. Coba lagi.")
        }

        // Panggil fungsi untuk mendapatkan pertanyaan berikutnya
        getQuestions()
    }

    private fun showToast(message: String) {
        // Tampilkan pesan toast ke pengguna
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
