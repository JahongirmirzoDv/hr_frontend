package uz.mobiledv.hr_frontend

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import uz.mobiledv.hr_frontend.ui.LoginScreen

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    println("Starting HR Frontend Application")
    ComposeViewport(document.body!!) {
        App()
    }
}