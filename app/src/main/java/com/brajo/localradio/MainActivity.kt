package com.brajo.localradio
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.UiContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstantState: Bundle?){
        super.onCreate(savedInstantState)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MusicPlayerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MusicPlayerScreen(){
    val context = LocalContext.current
    var songList by remember {mutableStateOf<List<Song>>(emptyList())}

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPlaying by remember {mutableStateOf<Song?>(null)}
    var isAudioPlaying by remember {mutableStateOf(false)}


    //Permission Logic
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    }else{
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission = audioPermission)



    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (permissionState.status.isGranted){
            LaunchedEffect(Unit) {
                val fetchedSongs = withContext(Dispatchers.IO){
                    fetchMusic(context)
                }

                songList = fetchedSongs
            }

            Column(modifier = Modifier.fillMaxSize()) {
                currentPlaying?.let {
                    song ->

                    var currentPosition by remember {mutableStateOf(0)}

                    LaunchedEffect(isAudioPlaying) {
                        while (isAudioPlaying && mediaPlayer != null){
                            currentPosition = mediaPlayer!!.currentPosition
                            delay(1000L)
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column() {
                            Row(
                                modifier = Modifier.padding( horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                Text(
                                    text = "Now Playing: ${song.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        if (isAudioPlaying) {
                                            mediaPlayer?.pause()
                                        } else {
                                            mediaPlayer?.start()
                                        }
                                        isAudioPlaying = !isAudioPlaying
                                    }
                                )
                                {
                                    Text(text = if (isAudioPlaying) "Pause" else "Play")
                                }
                            }

                            Column(
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Slider(
                                    value = currentPosition.toFloat(),
                                    valueRange = 0f..(song.duration.toFloat().coerceAtLeast(1f)),
                                    onValueChange = {
                                            draggedValue ->
                                        currentPosition = draggedValue.toInt()
                                    },

                                    onValueChangeFinished = {
                                        mediaPlayer?.seekTo(currentPosition)
                                    }
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ){
                                    Text(
                                        text = formatTime(currentPosition),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = formatTime(song.duration.toInt()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }


                    }
                }

                if(songList.isEmpty()){
                    Text("Scanning For Music")
                }
                else{
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(songList){ currentSong ->
                            SongListItem(
                                currentSong,
                                onClick = {
                                    mediaPlayer = playMusic(context, currentSong, mediaPlayer)
                                    currentPlaying = currentSong

                                    isAudioPlaying = true
                                }
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )

                        }
                    }
                }
            }

        } else {
            Text("We need permission to find your local music.")

            Button(
                onClick = {
                    permissionState.launchPermissionRequest()
                }) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun SongListItem(song: Song, onClick: () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{onClick()}
            .padding(16.dp)
    ) {
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )

        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}


fun fetchMusic(context: android.content.Context): List<Song> {
    val songs = mutableListOf<Song>()

    val collectionUri = if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    }else{
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DURATION
    )

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

    val query = context.contentResolver.query(
        collectionUri,
        projection,
        selection,
        null,
        "${MediaStore.Audio.Media.TITLE} ASC"
    )

    query?.use{
        cursor ->

        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

        while(cursor.moveToNext()){
            val id = cursor.getLong(idCol)
            val title = cursor.getString(titleCol)
            val artist = cursor.getString(artistCol)
            val path = cursor.getString(pathCol)
            val duration = cursor.getLong(durationCol)

            songs.add(Song(id,title,artist,path,duration))
        }
    }

    return songs
}


fun playMusic(context: Context, song:Song, currentPlayer: MediaPlayer?): MediaPlayer {
    currentPlayer?.release()
    val trackUri = ContentUris.withAppendedId(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        song.id
    )

    val newPlayer = MediaPlayer.create(context, trackUri)
    newPlayer.start()

    return newPlayer
}

@SuppressLint("DefaultLocale")
fun formatTime(milliseconds : Int): String {
    val totalSeconds = milliseconds/1000
    val minutes = totalSeconds/60
    val seconds = totalSeconds%60

    return String.format("%02d:%02d", minutes,seconds)
}

data class Song(
    val id : Long,
    val title : String,
    val artist : String,
    val path : String,
    val duration : Long
)

