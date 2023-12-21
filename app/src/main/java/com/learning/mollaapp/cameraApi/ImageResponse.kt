package com.learning.mollaapp.cameraApi

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageResponse(

	@field:SerializedName("translated_text")
	val translatedText: String? = null,

	@field:SerializedName("extracted_text")
	val extractedText: String? = null
) : Parcelable
