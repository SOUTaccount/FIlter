package ru.stebakov.profanityfilter

internal class DefaultReplacementFactory : ReplacementFactory {

    override fun createReplacement(profanity: String): String =
        buildString {
            repeat(profanity.length) { append('*') }
        }
}

interface ReplacementFactory {
    fun createReplacement(profanity: String): String
}