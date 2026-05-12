package com.example.tiktak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tiktak.di.AppModule
import com.example.tiktak.presentation.navigation.NavGraph
import com.example.tiktak.presentation.theme.DiaryTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    private val diaryRepository by lazy {
        (application as MyApplication).diaryRepository
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppModule.initialize(applicationContext)

        setContent {
            DiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Передаем репозиторий в NavGraph
                    NavGraph(diaryRepository = diaryRepository)
                }
            }
        }
    }
}