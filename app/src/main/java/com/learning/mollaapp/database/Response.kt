package com.learning.mollaapp.database

data class Response(
	val sentence: Sentence? = null,
	val questionId: String? = null
)

data class Sentence(
	val originalSentence: String? = null,
	val correctTranslation: String? = null,
	val shuffledSentence: String? = null
)

