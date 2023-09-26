package com.trm.opencoinmap.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val darkColorScheme: ColorScheme =
  darkColorScheme(
    primary = purple80,
    secondary = purpleGrey80,
    tertiary = pink80,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
  )

private val lightColorScheme: ColorScheme =
  lightColorScheme(
    primary = purple40,
    secondary = purpleGrey40,
    tertiary = pink40,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
  )

@Composable
fun OpenCoinMapComposeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = if (darkTheme) darkColorScheme else lightColorScheme,
    typography = Typography,
    content = content
  )
}
