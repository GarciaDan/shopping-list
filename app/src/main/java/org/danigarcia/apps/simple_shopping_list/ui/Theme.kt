package org.danigarcia.apps.simple_shopping_list.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Paleta armoniosa basada en verde (tema "lista de la compra")
private val GreenPrimary = Color(0xFF2E6C45)
private val GreenSecondary = Color(0xFF52634F)
private val GreenTertiary = Color(0xFF3B6470)
private val LightBackground = Color(0xFFF7FBF3)
private val DarkPrimary = Color(0xFF9CD6AC)

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB0F1C4),
    onPrimaryContainer = Color(0xFF00210C),
    secondary = GreenSecondary,
    tertiary = GreenTertiary,
    background = LightBackground,
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFDDE5DA)
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF00391D),
    primaryContainer = Color(0xFF175230),
    onPrimaryContainer = Color(0xFFB0F1C4),
    secondary = Color(0xFFBACCB3),
    tertiary = Color(0xFFA2CEDB),
    background = Color(0xFF101510),
    surface = Color(0xFF101510),
    surfaceVariant = Color(0xFF414940)
)

@Composable
fun ShoppingListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
