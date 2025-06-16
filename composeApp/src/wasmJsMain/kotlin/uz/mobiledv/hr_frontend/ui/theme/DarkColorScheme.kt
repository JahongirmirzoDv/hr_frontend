package uz.mobiledv.hr_frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Updated color scheme based on the mockups
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8A7FF0), // A slightly softer purple for primary actions
    background = Color(0xFF12141A), // A very dark, almost black background
    surface = Color(0xFF1C1F26), // The color for cards, dialogs, and main surfaces
    onPrimary = Color.White,
    onBackground = Color(0xFFE6E6E6), // Light gray text for general content
    onSurface = Color.White, // Light gray text on cards and surfaces
    surfaceVariant = Color(0xFF2C2F37), // Color for text fields, buttons, etc.
    outline = Color(0xFF3B3E46), // Border color for text fields and other elements
    error = Color(0xFFF87171) // A standard error color
)

@Composable
fun HrAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        // You can also define typography here to match the mockups
        typography = MaterialTheme.typography, // Using default for now, can be customized
        content = content
    )
}