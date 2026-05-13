package com.example.tiktak.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StGeorgeRibbon(
    modifier: Modifier = Modifier
) {
    // Цвета георгиевской ленты: черный и оранжевый
    val ribbonColors = listOf(
        Color(0xFF000000), // Черный
        Color(0xFFFF8C00), // Оранжевый
        Color(0xFF000000), // Черный
        Color(0xFFFF8C00), // Оранжевый
        Color(0xFF000000)  // Черный
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = ribbonColors,
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
    )
}

@Composable
fun StGeorgeRibbonVertical(
    modifier: Modifier = Modifier
) {
    val ribbonColors = listOf(
        Color(0xFF000000),
        Color(0xFFFF8C00),
        Color(0xFF000000),
        Color(0xFFFF8C00),
        Color(0xFF000000)
    )

    Box(
        modifier = modifier
            .width(6.dp)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = ribbonColors
                )
            )
    )
}

@Composable
fun StGeorgeRibbonDiagonal(
    modifier: Modifier = Modifier
) {
    val ribbonColors = listOf(
        Color(0xFF000000),
        Color(0xFFFF8C00),
        Color(0xFF000000),
        Color(0xFFFF8C00),
        Color(0xFF000000)
    )

    Box(
        modifier = modifier
            .size(50.dp)
            .rotate(45f)
            .background(
                brush = Brush.linearGradient(
                    colors = ribbonColors,
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
    )
}

@Composable
fun StGeorgeRibbonHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        StGeorgeRibbon()
        Spacer(modifier = Modifier.height(2.dp))
        StGeorgeRibbon()
    }
}