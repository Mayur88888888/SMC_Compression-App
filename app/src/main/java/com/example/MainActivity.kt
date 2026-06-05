package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.SmcCalculationRepository
import com.example.ui.SmcCalculationViewModel
import com.example.ui.SmcCalculationViewModelFactory
import com.example.ui.SmcCalculatorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize database layers
    val database = AppDatabase.getDatabase(applicationContext)
    val dao = database.smcCalculationDao()
    val repository = SmcCalculationRepository(dao)
    val factory = SmcCalculationViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[SmcCalculationViewModel::class.java]

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          SmcCalculatorScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
