package ru.stebakov.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.stebakov.mylibrary.AndroidProfanityFilter
import ru.stebakov.mylibrary.PlainDictionary

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class ExampleBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    val profanityFilter = AndroidProfanityFilter(dictionary = PlainDictionary(context = context, filePath = "profanity-words.txt"))

    @Test
    fun testCase1() {
        benchmarkRule.measureRepeated {
            profanityFilter.censor("Привет хуй манда жопа Привет хуй манда жопа Привет хуй манда жопаПривет хуй манда жопаПривет хуй манда жопа Привет хуй манда жопаПривет хуй манда жопаПривет хуй манда жопаПривет хуй манда жопаПривет хуй манда жопа")
        }
    }
}