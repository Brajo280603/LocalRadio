package com.brajo.localradio.ui.tabs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.brajo.localradio.PlaybackManager
import com.brajo.localradio.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun PlayerTab(
    song: Song?,
    isAudioPlaying: Boolean,
    context : Context
    ) {
    if(song == null){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text("No song Playing")
        }
        return
    }

    var currentPosition by remember {mutableIntStateOf(0)}
    var albumArt by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(song) {
        withContext(Dispatchers.IO){
            albumArt = getAlbumArt(song.path)
        }
    }

    LaunchedEffect(isAudioPlaying,song) {
        while (isAudioPlaying && PlaybackManager.mediaPlayer != null){
            currentPosition = PlaybackManager.mediaPlayer!!.currentPosition
            delay(500L)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ){
            if(albumArt != null){
                Image(
                    bitmap = albumArt!!.asImageBitmap(),
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.5f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.padding( horizontal = 16.dp, vertical = 8.dp),
        )
        {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        Slider(
            value = currentPosition.toFloat(),
            valueRange = 0f..(song.duration.toFloat().coerceAtLeast(1f)),
            onValueChange = {
                    draggedValue ->
                currentPosition = draggedValue.toInt()
            },

            onValueChangeFinished = {
                PlaybackManager.seekTo(context,currentPosition)
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = formatTime(song.duration.toInt()),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly

        )
        {
            Button(
                onClick = {
                    PlaybackManager.playPrevious(context)
                }
            )
            {
                Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            Button(
            onClick = {
                PlaybackManager.togglePlayPause(context)
            }
        )
            {
                Icon(imageVector = if(isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if(isAudioPlaying) "Pause" else "Play")
            }
            Button(
                onClick = {
                    PlaybackManager.playNext(context)
                }
            )
            {
                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next")
            }
        }


    }

}

@SuppressLint("DefaultLocale")
fun formatTime(milliseconds : Int): String {
    val totalSeconds = milliseconds/1000
    val minutes = totalSeconds/60
    val seconds = totalSeconds%60

    return String.format("%02d:%02d", minutes,seconds)
}

fun getAlbumArt(path: String): Bitmap?{
    return try{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val artBytes = retriever.embeddedPicture
        retriever.release()

        if(artBytes != null){
            BitmapFactory.decodeByteArray(artBytes,0,artBytes.size)
        }else{
            null
        }
    } catch (e: Exception){
        null
    }
}
