package com.example.mobile_kotlin;

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.mobile_kotlin.ui.navigation.NavGraph
import com.example.mobile_kotlin.ui.theme.Mobile_KotlinTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import com.example.mobile_kotlin.viewmodels.AuthViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mobile_KotlinTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}