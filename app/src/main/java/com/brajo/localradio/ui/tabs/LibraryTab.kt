package com.brajo.localradio.ui.tabs

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brajo.localradio.Song

@Composable
fun LibraryTab(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    songList: List<Song>,
    currentPlaying: Song?,
    isAudioPlaying: Boolean,
    onSongClick: (Song) -> Unit
    ) {

    Column(modifier = Modifier.fillMaxSize()) {
        LibrarySearchBar(searchQuery,onSearchQueryChange,Modifier.padding(16.dp).fillMaxWidth())


        if(songList.isEmpty() && searchQuery.isEmpty()){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Text("Scanning For Music")
            }
        } else if (songList.isEmpty() && searchQuery.isNotEmpty()){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Text("No songs found for \"${searchQuery}\"")
            }
        }
        else{
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(songList){ currentSong ->
                    val isCurrentlyPlaying = currentPlaying?.id == currentSong.id

                    SongListItem(
                        currentSong,
                        isCurrentSong = isCurrentlyPlaying,
                        isPlaying = isCurrentlyPlaying && isAudioPlaying,
                        onClick = {onSongClick(currentSong)}
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                }
            }
        }
    }
}

@Composable
fun SongListItem(song: Song,isCurrentSong:Boolean,isPlaying:Boolean, onClick: () -> Unit){
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable{onClick()}
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Column(
            modifier = Modifier.weight(1f)
        )
        {
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

        if(isCurrentSong){
            PlayingEqualizerIcon(isAnimating = isPlaying)
        }
    }


}


@Composable
fun LibrarySearchBar(
    query:String,
    onQueryChange: (String)->Unit,
    modifier: Modifier = Modifier
){
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {Text("Search songs or artists...")},
        leadingIcon = {Icon(Icons.Default.Search, contentDescription = "Search Icon")},
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(40.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun PlayingEqualizerIcon(isAnimating:Boolean){
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if(isAnimating) 1f else 0.4f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse),
        label = "bar1"
    )

    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if(isAnimating) 0.8f else 0.2f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "bar2"
    )

    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = if(isAnimating) 1f else 0.5f,
        animationSpec = infiniteRepeatable(tween(250), RepeatMode.Reverse),
        label = "bar3"
    )

    Row(
        modifier = Modifier.height(16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ){
        val color = MaterialTheme.colorScheme.primary
        Box(modifier = Modifier.width(3.dp).fillMaxHeight(bar1).background(color))
        Box(modifier = Modifier.width(3.dp).fillMaxHeight(bar2).background(color))
        Box(modifier = Modifier.width(3.dp).fillMaxHeight(bar3).background(color))
    }
}