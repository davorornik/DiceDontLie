package si.ornik.dicedontlie.ui.theme

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
import si.ornik.dicedontlie.data.EventDie

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Gray900,
    surface = Gray800,
    surfaceVariant = Gray700,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = White,
    error = Error,
    onError = White,
    outline = Gray400,
    outlineVariant = Gray500
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = White,
    surface = White,
    surfaceVariant = Gray100,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Black,
    onSurface = Black,
    onSurfaceVariant = Black,
    error = Error,
    onError = White,
    outline = Gray400,
    outlineVariant = Gray300
)

@Composable
fun DiceDontLieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun getEventDieColor(eventDie: EventDie, darkTheme: Boolean = isSystemInDarkTheme()): Color {
    return when (eventDie) {
        EventDie.POLITICS -> EventDieBlue
        EventDie.SCIENCE -> EventDieGreen
        EventDie.TRADE -> EventDieYellow
        EventDie.PIRATES -> if (darkTheme) White else EventDieBlack

    }
}