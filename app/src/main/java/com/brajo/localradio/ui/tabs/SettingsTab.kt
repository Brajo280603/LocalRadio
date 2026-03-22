package com.brajo.localradio.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsTab(
    isAutoplay: Boolean,
    isRadioMode: Boolean,
    onAutoplayToggle: () -> Unit,
    onRadioToggle: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Text("Settings tab coming soon")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Playback Preferences", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onAutoplayToggle)
        {
            Text(text = if (isAutoplay) "Autoplay Off" else "Autoplay On")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRadioToggle)
        {
            Text(text = if (isRadioMode) "Radio Off" else "Radio On")
        }
    }
}