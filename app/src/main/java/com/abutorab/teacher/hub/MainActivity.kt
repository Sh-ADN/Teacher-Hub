package com.abutorab.teacher.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abutorab.teacher.hub.data.AppDatabase
import com.abutorab.teacher.hub.data.AppRepository
import com.abutorab.teacher.hub.domain.TeacherViewModel
import com.abutorab.teacher.hub.domain.TeacherViewModelFactory
import com.abutorab.teacher.hub.ui.screens.MainScreen
import com.abutorab.teacher.hub.ui.theme.MyApplicationTheme

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
