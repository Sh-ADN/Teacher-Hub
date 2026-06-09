package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.domain.TeacherViewModel
import com.example.domain.TeacherViewModelFactory
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val db = AppDatabase.getDatabase(applicationContext)
    val repository = AppRepository(db.appDao())
    
    setContent {
      MyApplicationTheme {
        val viewModel: TeacherViewModel = viewModel(factory = TeacherViewModelFactory(repository))
        MainScreen(viewModel)
      }
    }
  }
}
