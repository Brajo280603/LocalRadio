package com.brajo.localradio.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brajo.localradio.Song

@Composable
fun LibraryTab(
    songList: List<Song>,
    currentPlaying: Song?,
    onSongClick: (Song) -> Unit
    ) {

    if(songList.isEmpty()){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text("Scanning For Music")
        }
    }
    else{
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(songList){ currentSong ->
                SongListItem(
                    currentSong,
                    onClick = {onSongClick(currentSong)}
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant
                )

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

