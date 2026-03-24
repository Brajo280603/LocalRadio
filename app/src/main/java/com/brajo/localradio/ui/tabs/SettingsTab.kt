package com.brajo.localradio.ui.tabs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.brajo.localradio.AppDatabase
import com.brajo.localradio.AppSettings
import com.brajo.localradio.DatabaseUtils
import com.brajo.localradio.MusicService
import com.brajo.localradio.PlaybackManager
import com.brajo.localradio.PlaybackManager.isClassicRadioModeActive
import com.brajo.localradio.PlaybackMode
import com.brajo.localradio.Song
import kotlinx.coroutines.launch
import kotlin.contracts.contract
import kotlin.system.exitProcess

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

//        Spacer(modifier = Modifier.padding(top=12.dp))
        AiEngineProgressCard()
        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ){
            DatabaseExportButton()
            DatabaseImportButtton()
        }

        HorizontalDivider()


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
            HorizontalDivider()
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

@Composable
fun AiEngineProgressCard(){
    val context = LocalContext.current
    val dao = remember{AppDatabase.getDatabase(context).songDao()}

    val analyzedCount by dao.getAnalyzedSongsCount().collectAsState(0)
    val totalCount by dao.getTotalSongsCount().collectAsState(0)

    val progress = if(totalCount > 0) analyzedCount.toFloat()/ totalCount.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Column(modifier = Modifier.padding(16.dp)){
            Text(
                text = "Ai Engine Status",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = "Library Scanned: $analyzedCount / $totalCount songs",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.padding(8.dp))

            LinearProgressIndicator(
                progress = {progress},
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )

            if(analyzedCount == totalCount && totalCount > 0){
                Text(
                    text = "Analysis Complete! The DJ is ready.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

    }
}

@Composable
fun DatabaseExportButton(){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var exportStatus by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-sqlite3")
    ) { uri ->
        if(uri != null){
            exportStatus = "Exporting..."
            coroutineScope.launch {
                val success = DatabaseUtils.exportDatabase(context, uri)
                exportStatus = if (success) "Export Successfull!" else "Export Failed. Check logs."
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                exportLauncher.launch("backup_local-radio-db.backup")
            },
//            modifier = Modifier.fillMaxWidth()
        )
        {
            Text("Export Database")
        }

        if(exportStatus.isNotEmpty()){
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = exportStatus, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DatabaseImportButtton(){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var importStatus by remember { mutableStateOf("") }
    var showRestartDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    )  { uri ->
        if (uri != null){
            importStatus = "Importing Database..."
            coroutineScope.launch {
                val success = DatabaseUtils.importDatabase(context, uri)
                if(success){
                    importStatus = "Successful!"
                    showRestartDialog = true
                }else{
                    importStatus = "Import Failed. Check Logs."
                }
            }
        }
    }


    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                importLauncher.launch(arrayOf("*/*"))
            },
//            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Import Database (Overrides Current Data)")
        }

        if(importStatus.isNotEmpty()){
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = importStatus, style = MaterialTheme.typography.bodySmall)
        }
    }

    if(showRestartDialog){
        AlertDialog(
            onDismissRequest = {},
            title = {Text("Restart Required")},
            text = {Text("the database was successfully replaced. The app needs a restart")},
            confirmButton = {
                Button(onClick = {
                    exitProcess(0)
                }) {
                    Text("Restart App")
                }
            }

        )
    }
}