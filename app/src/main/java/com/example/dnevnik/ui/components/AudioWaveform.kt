package com.example.dnevnik.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun AudioWaveform(
    isRecording: Boolean,
    amplitude: Int = 0,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val waveAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isRecording) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val barCount = 20
        val barWidth = size.width / (barCount * 2)
        val maxBarHeight = size.height

        for (i in 0 until barCount) {
            val normalizedAmplitude = if (isRecording) {
                (amplitude / 32767f) * (Random.nextFloat() * 0.5f + 0.5f)
            } else {
                0.1f
            }

            val barHeight = maxBarHeight * normalizedAmplitude * waveAnimation
            val x = i * (barWidth * 2) + barWidth

            drawRect(
                color = Color(0xFF6750A4),
                topLeft = Offset(x, (size.height - barHeight) / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}