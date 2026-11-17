package com.egebey.mineplace.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MineplaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFF00E676),
        secondary = Color(0xFF00E5FF),
        background = Color(0xFF0A0D12),
        surface = Color(0xFF131922)
    ), content = content)
}
