package com.example.tiktak.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tiktak.domain.model.Emotion

@Composable
fun EmotionSelector(
    selectedEmotion: Emotion,
    onEmotionSelected: (Emotion) -> Unit
) {
    Column {
        Text(
            text = "Как вы себя чувствуете?",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Emotion.values().toList()) { emotion ->
                FilterChip(
                    selected = selectedEmotion == emotion,
                    onClick = { onEmotionSelected(emotion) },
                    label = { Text("${emotion.emoji} ${emotion.displayName.lowercase().replaceFirstChar { it.uppercase() }}") },
                    modifier = Modifier.height(40.dp)
                )
            }
        }
    }
}