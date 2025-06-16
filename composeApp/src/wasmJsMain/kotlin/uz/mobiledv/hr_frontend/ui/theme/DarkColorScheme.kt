// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/ui/theme/Theme.kt
package uz.mobiledv.hr_frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6A5AE0), // A purple/blue accent from the mockups
    background = Color(0xFF1A1C24), // The main dark background
    surface = Color(0xFF232631), // The color for cards and surfaces
    onPrimary = Color.White,
    onBackground = Color(0xFFE4E4E6), // Light gray text for backgrounds
    onSurface = Color(0xFFE4E4E6), // Light gray text for surfaces
    surfaceVariant = Color(0xFF303442), // Color for text fields, etc.
    outline = Color(0xFF4A4D59)
)

@Composable
fun HrAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        // You can also define typography here to match the mockups
        content = content
    )
}