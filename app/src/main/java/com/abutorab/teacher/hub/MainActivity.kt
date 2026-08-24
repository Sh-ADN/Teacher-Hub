package com.abutorab.teacher.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abutorab.teacher.hub.domain.TeacherViewModel
import com.abutorab.teacher.hub.ui.screens.MainScreen
import com.abutorab.teacher.hub.ui.screens.TermPickerScreen
import com.abutorab.teacher.hub.ui.screens.YearPickerScreen
import com.abutorab.teacher.hub.ui.theme.TeacherHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TeacherViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            TeacherHubTheme(darkTheme = isDark) {
                var pickedYear by remember { mutableStateOf<Int?>(null) }
                var pickedTerm by remember { mutableStateOf<String?>(null) }

                when {
                    pickedYear == null -> {
                        YearPickerScreen(onYearSelected = { year ->
                            pickedYear = year
                        })
                    }
                    pickedTerm == null -> {
                        TermPickerScreen(
                            selectedYear = pickedYear!!,
                            onTermSelected = { term ->
                                pickedTerm = term
                                viewModel.setYearAndTerm(pickedYear!!, term)
                            },
                            onBack = { pickedYear = null }
                        )
                    }
                    else -> {
                        MainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
