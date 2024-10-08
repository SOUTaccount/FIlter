package ru.stebakov.profanityfilter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import ru.stebakov.profanityfilter.ui.theme.ProfanityFilterTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val profanityFilter = LocalProfanityFilter.current


            val text = "Привет хуй манда жопа Привет хуй манда жопа Привет хуй манда жопаПривет хуй манда жопаПривет хуй манда жопа Привет хуй манда жопаПривет хуй манда жопаПривет хуй манда жопаПривет хуй манда жопаПривет хуй манда жопа"
            ProfanityFilterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(Modifier.fillMaxSize()) {
                        val textState = remember {
                            mutableStateOf(TextFieldValue(""))
                        }
                        val textState1 = remember {
                            mutableStateOf("")
                        }
                        Text(text = "Hello")
                        OutlinedTextField(
                            value = textState.value,
                            onValueChange = { textState.value = it }
                        )
                        Button(onClick = {
                            textState1.value = profanityFilter.censor(text)
                        }) {
                            Text("click me")
                        }
                        Text(text = textState1.value)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProfanityFilterTheme {
        Greeting("Android")
    }
}