package ru.stebakov.mylibrary

interface Dictionary {
    val size: Int
    fun search(text: String): Set<String>
}