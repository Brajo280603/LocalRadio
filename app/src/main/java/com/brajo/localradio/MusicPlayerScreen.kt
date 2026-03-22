//package com.brajo.localradio
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.os.Build
//import android.provider.MediaStore
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Button
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Slider
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.room.Room
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.isGranted
//import com.google.accompanist.permissions.rememberPermissionState
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.withContext
//
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun MusicPlayerScreen(){
//    val context = LocalContext.current
//
//    val db = remember {
//        Room.databaseBuilder(
//            context.applicationContext,
//            AppDatabase::class.java, "local-radio-db"
//        )
//            .fallbackToDestructiveMigration()
//            .build()
//    }
//
//    val songDao = db.songDao()
//    val songList by songDao.getAllSongs().collectAsState(initial = emptyList())
//
//
//    var currentPlaying by remember {mutableStateOf<Song?>(null)}
//    var isAudioPlaying by remember {mutableStateOf(false)}
//    var isAutoplay by remember {mutableStateOf(false)}
//    var isRadioMode by remember {mutableStateOf(false)}
//
//
//    //Permission Logic
//    val audioPermission:String
//    val notificationPermission:String
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        audioPermission = Manifest.permission.READ_MEDIA_AUDIO
//        notificationPermission = Manifest.permission.POST_NOTIFICATIONS
//        val notificationPermissionState = rememberPermissionState(notificationPermission)
//        LaunchedEffect(Unit) {
//            if (!notificationPermissionState.status.isGranted){
//                notificationPermissionState.launchPermissionRequest()
//            }
//        }
//    }else{
//        audioPermission = Manifest.permission.READ_EXTERNAL_STORAGE
//    }
//
//    val audioPermissionState = rememberPermissionState(permission = audioPermission)
//
//
//
//
//
//
//
//
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        if (audioPermissionState.status.isGranted){
//            LaunchedEffect(Unit) {
//                val fetchedSongs = withContext(Dispatchers.IO){ fetchMusic(context) }
//                songDao.insertAll(fetchedSongs)
//            }
//
//            Column(modifier = Modifier.fillMaxSize()) {
//                currentPlaying?.let {
//                        song ->
//
//                    var currentPosition by remember {mutableIntStateOf(0)}
//
//                    LaunchedEffect(isAudioPlaying,song) {
//                        while (isAudioPlaying && PlaybackManager.mediaPlayer != null){
//                            currentPosition = PlaybackManager.mediaPlayer!!.currentPosition
//                            delay(1000L)
//                        }
//                    }
//                    Surface(
//                        modifier = Modifier.fillMaxWidth(),
//                        color = MaterialTheme.colorScheme.primaryContainer
//                    ) {
//                        Column{
//                            Row(
//                                modifier = Modifier.padding( horizontal = 16.dp, vertical = 8.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            )
//                            {
//                                Text(
//                                    text = "Now Playing: ${song.title}",
//                                    style = MaterialTheme.typography.titleMedium,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
//                                    maxLines = 1,
//                                    modifier = Modifier.weight(1f)
//                                )
//
//                                Button(
//                                    onClick = {
//                                        PlaybackManager.togglePlayPause(context)
//                                    }
//                                )
//                                {
//                                    Text(text = if (isAudioPlaying) "Pause" else "Play")
//                                }
//                            }
//
//                            Column(
//                                modifier = Modifier.padding(top = 8.dp)
//                            ) {
//                                Slider(
//                                    value = currentPosition.toFloat(),
//                                    valueRange = 0f..(song.duration.toFloat().coerceAtLeast(1f)),
//                                    onValueChange = {
//                                            draggedValue ->
//                                        currentPosition = draggedValue.toInt()
//                                    },
//
//                                    onValueChangeFinished = {
//                                       PlaybackManager.seekTo(context,currentPosition)
//                                    }
//                                )
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(horizontal = 8.dp),
//                                    horizontalArrangement = Arrangement.SpaceBetween
//                                )
//                                {
//                                    Text(
//                                        text = formatTime(currentPosition),
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                                    )
//                                    Text(
//                                        text = formatTime(song.duration.toInt()),
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                                    )
//                                }
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(horizontal = 8.dp),
//                                    horizontalArrangement = Arrangement.SpaceEvenly
//                                )
//                                {
//                                    Button(
//                                        onClick = {
//                                            isAutoplay = !isAutoplay
//                                            if(isAutoplay) isRadioMode = false
//
//                                            PlaybackManager.isAutoplay = isAutoplay
//                                            PlaybackManager.isRadioMode = isRadioMode
//                                        }
//                                    )
//                                    {
//                                        Text(text = if (isAutoplay) "Autoplay Off" else "Autoplay On")
//                                    }
//
//                                    Button(
//                                        onClick = {
//                                            isRadioMode = !isRadioMode
//                                            if(isRadioMode) isAutoplay = false
//
//                                            PlaybackManager.isAutoplay = isAutoplay
//                                            PlaybackManager.isRadioMode = isRadioMode
//                                        }
//                                    )
//                                    {
//                                        Text(text = if (isRadioMode) "Radio Off" else "Radio On")
//                                    }
//                                }
//                            }
//                        }
//
//
//                    }
//                }
//
//                if(songList.isEmpty()){
//                    Text("Scanning For Music")
//                }
//                else{
//                    LazyColumn(modifier = Modifier.weight(1f)) {
//                        items(songList){ currentSong ->
//                            SongListItem(
//                                currentSong,
//                                onClick = {
//
//                                    PlaybackManager.currentSongList = songList
//
//                                    PlaybackManager.playTrack(
//                                        context = context,
//                                        song = currentSong,
//                                        isManualClick = true,
//                                        onUpdateUI = { song, isPlaying ->
//                                            currentPlaying = song
//                                            isAudioPlaying = isPlaying
//                                        }
//                                    )
//                                }
//                            )
//
//                            HorizontalDivider(
//                                color = MaterialTheme.colorScheme.surfaceVariant
//                            )
//
//                        }
//                    }
//                }
//            }
//
//        }
//        else {
//            Text("We need permission to find your local music.")
//
//            Button(
//                onClick = {
//                    audioPermissionState.launchPermissionRequest()
//                }) {
//                Text("Grant Permission")
//            }
//        }
//    }
//}
//
//@Composable
//fun SongListItem(song: Song, onClick: () -> Unit){
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable{onClick()}
//            .padding(16.dp)
//    ) {
//        Text(
//            text = song.title,
//            style = MaterialTheme.typography.titleMedium,
//            maxLines = 1
//        )
//
//        Text(
//            text = song.artist,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            maxLines = 1
//        )
//    }
//}
//
//
//fun fetchMusic(context: android.content.Context): List<Song> {
//    val songs = mutableListOf<Song>()
//
//    val collectionUri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
//    }else{
//        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//    }
//
//    val projection = arrayOf(
//        MediaStore.Audio.Media._ID,
//        MediaStore.Audio.Media.TITLE,
//        MediaStore.Audio.Media.ARTIST,
//        MediaStore.Audio.Media.DATA,
//        MediaStore.Audio.Media.DURATION
//    )
//
//    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
//
//    val query = context.contentResolver.query(
//        collectionUri,
//        projection,
//        selection,
//        null,
//        "${MediaStore.Audio.Media.TITLE} ASC"
//    )
//
//    query?.use{
//            cursor ->
//
//        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
//        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
//        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
//        val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
//        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
//
//        while(cursor.moveToNext()){
//            val id = cursor.getLong(idCol)
//            val title = cursor.getString(titleCol)
//            val artist = cursor.getString(artistCol)
//            val path = cursor.getString(pathCol)
//            val duration = cursor.getLong(durationCol)
//
//            val fakeVector = List(5){Math.random().toFloat()}.joinToString(",")
//
//            songs.add(Song(id,title,artist,path,duration,fakeVector))
//        }
//    }
//
//    return songs
//}
//
//@SuppressLint("DefaultLocale")
//fun formatTime(milliseconds : Int): String {
//    val totalSeconds = milliseconds/1000
//    val minutes = totalSeconds/60
//    val seconds = totalSeconds%60
//
//    return String.format("%02d:%02d", minutes,seconds)
//}
//
