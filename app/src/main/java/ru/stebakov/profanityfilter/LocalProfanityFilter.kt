package ru.stebakov.profanityfilter

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale

val LocalProfanityFilter = ProfanityFilterProvidableCompositionLocal()

@JvmInline
value class ProfanityFilterProvidableCompositionLocal internal constructor(
    private val delegate: ProvidableCompositionLocal<ProfanityFilter?> =
        staticCompositionLocalOf { null }
) {

    val current: ProfanityFilter
        @Composable get() = delegate.current ?: LocalContext.current.profanityFilter

    infix fun provides(value: ProfanityFilter) = delegate provides value

    infix fun providesDefault(value: ProfanityFilter) = delegate providesDefault value
}

inline val Context.profanityFilter: ProfanityFilter
    @JvmName("profanityFilter") get() {
        val dictionary = when (Locale.current) {
            Locale("en-US") -> PlainDictionary(this, "profanity-words.txt")
            else -> PlainDictionary(this, "profanity-words.txt")
        }
        return AndroidProfanityFilter(dictionary = dictionary)
    }