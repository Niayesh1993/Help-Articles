package com.zozi.helparticlesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zozi.helparticlesapp.navigation.AppNavigation
import com.zozi.helparticlesapp.ui.theme.HelpArticlesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpArticlesTheme {
                AppNavigation()
            }
        }
    }
}
