package uz.mobiledv.hr_frontend

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.context.GlobalContext.startKoin
import uz.mobiledv.hr_frontend.di.appModule
import uz.mobiledv.hr_frontend.ui.LoginScreen

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    println("Starting HR Frontend Application")
    startKoin {
        modules(appModule)
    }
    ComposeViewport(document.body!!) {
        App()
    }
}