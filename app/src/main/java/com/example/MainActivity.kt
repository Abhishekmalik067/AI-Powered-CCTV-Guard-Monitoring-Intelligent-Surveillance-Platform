package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme

import androidx.activity.viewModels
import com.example.ui.CctvViewModel
import com.example.ui.CctvViewModelFactory
import com.example.ui.SentryDashboard

class MainActivity : ComponentActivity() {
  private val viewModel: CctvViewModel by viewModels {
    val app = application as CctvApplication
    CctvViewModelFactory(app, app.repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        SentryDashboard(viewModel)
      }
    }
  }
}
