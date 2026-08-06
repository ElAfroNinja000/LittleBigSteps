package com.littlebigsteps.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.littlebigsteps.app.ui.theme.LittleBigStepsTheme

/**
 * Point d'entrée unique de l'app (une seule Activity, navigation gérée en Compose).
 * Écran placeholder en attendant onboarding/sélection de défi/portfolio.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LittleBigStepsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlaceholderHome(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun PlaceholderHome(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Text(
            text = "LittleBigSteps — squelette en place 🌱",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderHomePreview() {
    LittleBigStepsTheme {
        PlaceholderHome()
    }
}
