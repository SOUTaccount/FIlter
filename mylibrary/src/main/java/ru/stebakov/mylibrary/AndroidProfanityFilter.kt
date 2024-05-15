package ru.stebakov.mylibrary

import android.util.Log

open class AndroidProfanityFilter(
    private val dictionary: Dictionary,
    private val replacementFactory: ReplacementFactory = DefaultReplacementFactory()
) : ProfanityFilter {

    override fun censor(text: String): String {
        Log.d("ASFSAFA","INSERT REQUEST")
        val badWords = dictionary.search(text)
        Log.d("ASFSAFA","REQUEST END")
        return if (badWords.isEmpty()) text else clearProfanityWords(text, badWords)
    }

    private fun clearProfanityWords(text: String, badWords: Set<String>): String {
        var newText = text
        badWords.forEach { word ->
            val badWord = word.removeSpaces().lowercase()
            val replacement = replacementFactory.createReplacement(badWord)
            newText = newText.replaceCompound(badWord, replacement)
        }
        return newText
    }
}
interface ProfanityFilter {

    fun censor(text: String): String
}