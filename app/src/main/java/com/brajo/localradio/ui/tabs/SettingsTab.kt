package com.brajo.localradio.ui.tabs

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import com.brajo.localradio.AppSettings
import com.brajo.localradio.MusicService
import com.brajo.localradio.PlaybackManager
import com.brajo.localradio.PlaybackManager.isClassicRadioModeActive
import com.brajo.localradio.PlaybackMode
import com.brajo.localradio.Song

@Composable
fun SettingsTab(
    context: Context,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit
    ) {
    Column(
        modifier = Modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.padding(top=8.dp))
        Text("Settings", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(16.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Playback Preferences", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(32.dp))

            RadioOptionRow("Autoplay","Autoplay the next song in library",settings.playbackMode==PlaybackMode.SEQUENTIAL, enabled = !settings.isClassicRadioMode, onClick = {
                PlaybackManager.currentMode = PlaybackMode.SEQUENTIAL
                onSettingsChange(settings.copy(playbackMode = PlaybackMode.SEQUENTIAL))
            })
            HorizontalDivider()
            RadioOptionRow("Radio","Plays Similar songs",settings.playbackMode==PlaybackMode.RADIO, enabled = !settings.isClassicRadioMode, onClick = {
                PlaybackManager.currentMode = PlaybackMode.RADIO
                onSettingsChange(settings.copy(playbackMode = PlaybackMode.RADIO))
            })
            HorizontalDivider()
            RadioOptionRow("Random","Plays Random songs ¯\\_(ツ)_/¯",settings.playbackMode==PlaybackMode.RANDOM, enabled = !settings.isClassicRadioMode, onClick = {
                PlaybackManager.currentMode = PlaybackMode.RANDOM
                onSettingsChange(settings.copy(playbackMode = PlaybackMode.RANDOM))
            })
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            SettingsToggleRow(
                title = "CLASSIC RADIO MODE",
                description = "Disable skipping and seeking, Trust the curations",
                value = settings.isClassicRadioMode,
                onValueChanged = { newValue ->

                    notificationSync(PlaybackManager.currentSong,context,newValue)
                    PlaybackManager.isClassicRadioModeActive = newValue
                    if(newValue){
                        PlaybackManager.currentMode = PlaybackMode.RADIO
                        onSettingsChange(settings.copy(isClassicRadioMode = true, playbackMode = PlaybackMode.RADIO))
                    }
                    else{
                        onSettingsChange(settings.copy(isClassicRadioMode = false))
                    }
                }
            )
        }


    }
}

@Composable
fun RadioOptionRow(title: String, description: String, selected:Boolean, enabled: Boolean, onClick:() -> Unit){
    val itemAlpha = if (enabled) 1f else 0.5f

    ListItem(
        modifier = Modifier.clickable(enabled = enabled){onClick()}.alpha(itemAlpha),
        headlineContent = {Text(title)},
        supportingContent = {Text(description)},
        trailingContent = {
            RadioButton(
                selected = selected,
                enabled = enabled,
                onClick = onClick
            )
        }
    )
}

//For Future Features
@Composable
fun SettingsToggleRow(title: String, description: String, value:Boolean, onValueChanged:(Boolean) -> Unit){
    ListItem(
        headlineContent = {Text(title)},
        supportingContent = {Text(description)},
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = {onValueChanged(it)}
            )
        }
    )
}


fun notificationSync(currentSong: Song?,context: Context,isClassicMode: Boolean){
    currentSong?.let { song ->
        val serviceIntent = Intent(context, MusicService::class.java).apply {
            putExtra("SONG_TITLE", song.title)
            putExtra("SONG_ARTIST", song.artist)
            putExtra("SONG_DURATION", song.duration)
            putExtra("SONG_PATH",song.path)
            putExtra("IS_CLASSIC_MODE",isClassicMode)
        }
        context.startForegroundService(serviceIntent)
    }
}