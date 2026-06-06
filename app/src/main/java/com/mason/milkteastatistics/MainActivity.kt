package com.mason.milkteastatistics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mason.milkteastatistics.ui.navigation.AppNavigation
import com.mason.milkteastatistics.ui.theme.MilkTeaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MilkTeaTheme {
                AppNavigation()
            }
        }
    }
}
