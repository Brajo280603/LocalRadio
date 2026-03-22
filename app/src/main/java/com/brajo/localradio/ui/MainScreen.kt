package com.brajo.localradio.ui

import android.Manifest
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.brajo.localradio.AppDatabase
import com.brajo.localradio.PlaybackManager
import com.brajo.localradio.Song
import com.brajo.localradio.ui.tabs.LibraryTab
import com.brajo.localradio.ui.tabs.PlayerTab
import com.brajo.localradio.ui.tabs.SettingsTab
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.emptyList

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(){
    val context = LocalContext.current

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "local-radio-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    val songDao = db.songDao()
    val songList by songDao.getAllSongs().collectAsState(initial = emptyList())

    var currentPlaying by remember { mutableStateOf<Song?>(PlaybackManager.currentSong) }
    var isAudioPlaying by remember { mutableStateOf(PlaybackManager.mediaPlayer?.isPlaying == true) }
    var isAutoplay by remember { mutableStateOf(PlaybackManager.isAutoplay) }
    var isRadioMode by remember { mutableStateOf(PlaybackManager.isRadioMode) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        PlaybackManager.uiUpdateCallback = {song, isPlaying->
            currentPlaying = song
            isAudioPlaying = isPlaying
        }
    }

    val audioPermission:String
    val notificationPermission:String
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        audioPermission = Manifest.permission.READ_MEDIA_AUDIO
        notificationPermission = Manifest.permission.POST_NOTIFICATIONS
        val notificationPermissionState = rememberPermissionState(notificationPermission)
        LaunchedEffect(Unit) {
            if (!notificationPermissionState.status.isGranted){
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }else{
        audioPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val audioPermissionState = rememberPermissionState(permission = audioPermission)


    if (audioPermissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            val fetchedSongs = withContext(Dispatchers.IO) { fetchMusic(context) }
            songDao.insertAll(fetchedSongs)
        }

        val tabTitles = listOf("Library", "Now Playing", "Settings")
        val tabIcons = listOf(Icons.AutoMirrored.Filled.List, Icons.Default.PlayArrow, Icons.Default.Settings)


        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabTitles.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = { Icon(imageVector = tabIcons[index], title) },
                            label = { Text(text = title) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTabIndex) {
                    0 -> LibraryTab(
                        songList = songList,
                        currentPlaying = currentPlaying,
                        onSongClick = {song ->
                            PlaybackManager.currentSongList = songList
                            PlaybackManager.playTrack(
                                context = context,song = song, isManualClick = true,
                                onUpdateUI = { updatedSong ,isPlaying->
                                    currentPlaying = updatedSong
                                    isAudioPlaying = isPlaying
                                }
                            )
                        }
                    )

                    1 -> PlayerTab(
                        song = currentPlaying,
                        isAudioPlaying = isAudioPlaying,
                        context = context
                    )

                    2 -> SettingsTab(
                        isAutoplay = isAutoplay, isRadioMode = isRadioMode,
                        onAutoplayToggle = {
                            isAutoplay = !isAutoplay
                            if(isAutoplay) isRadioMode = false
                            PlaybackManager.isAutoplay = isAutoplay
                            PlaybackManager.isRadioMode = isRadioMode
                        },
                        onRadioToggle = {
                            isRadioMode = !isRadioMode
                            if(isRadioMode) isAutoplay = false
                            PlaybackManager.isAutoplay = isAutoplay
                            PlaybackManager.isRadioMode = isRadioMode
                        }
                    )
                }
            }
        }

    }
    else {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text("We need permission to find your local music.")

            Button(
                onClick = {
                    audioPermissionState.launchPermissionRequest()
                }) {
                Text("Grant Permission")
            }
        }

    }
}


fun fetchMusic(context: android.content.Context): List<Song> {
    val songs = mutableListOf<Song>()

    val collectionUri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
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

            val fakeVector = List(5){Math.random().toFloat()}.joinToString(",")

            songs.add(Song(id,title,artist,path,duration,fakeVector))
        }
    }

    return songs
}
