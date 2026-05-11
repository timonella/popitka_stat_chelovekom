package com.example.dnevnik.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.dnevnik.data.local.dao.EmotionStat

@Composable
fun EmotionPieChart(
    stats: List<EmotionStat>,
    modifier: Modifier = Modifier
) {
    val emotionColors = mapOf(
        "Счастливый" to Color(0xFFFFD700),
        "Грустный" to Color(0xFF4169E1),
        "Тревожный" to Color(0xFFFF6347),
        "Спокойный" to Color(0xFF98FB98),
        "Злой" to Color(0xFFFF4500),
        "Нейтральный" to Color(0xFFD3D3D3),
        "Взволнованный" to Color(0xFFFF69B4),
        "Уставший" to Color(0xFF8A2BE2)
    )

    val totalCount = stats.sumOf { it.count }.toFloat()

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            var startAngle = -90f

            for (stat in stats) {
                val sweepAngle = (stat.count / totalCount) * 360f
                val color = emotionColors[stat.emotion] ?: Color.Gray

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(size.width / 2, size.height.toFloat()),
                    topLeft = Offset(size.width / 4, 0f)
                )

                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 2f),
                    size = Size(size.width / 2, size.height.toFloat()),
                    topLeft = Offset(size.width / 4, 0f)
                )

                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Легенда
        for (stat in stats) {
            val color = emotionColors[stat.emotion] ?: Color.Gray
            val percentage = ((stat.count / totalCount) * 100).toInt()

            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawCircle(color = color)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${stat.emotion}: $percentage%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}