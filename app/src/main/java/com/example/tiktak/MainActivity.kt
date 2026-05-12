package com.example.tiktak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tiktak.di.AppModule
import com.example.tiktak.presentation.navigation.NavGraph
import com.example.tiktak.presentation.theme.DiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppModule.initialize(applicationContext)

        setContent {
            DiaryTheme {
                NavGraph()
            }
        }
    }
}