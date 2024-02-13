package ru.stebakov.profanityfilter

interface Dictionary {
    val size: Int
    fun search(text: String): Set<String>
}