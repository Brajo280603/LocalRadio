package com.brajo.localradio

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.provider.MediaStore

object PlaybackManager {
    var mediaPlayer : MediaPlayer? = null
    var isAutoplay : Boolean = false
    var isRadioMode : Boolean = false
    var currentSongList: List<Song> = emptyList()

    private var playedHistory = mutableSetOf<Long>()

    var currentSong: Song? = null
    var uiUpdateCallback: ((Song,Boolean) -> Unit)? = null
    fun playTrack(
        context:Context,
        song: Song,
        isManualClick:Boolean = false,
        onUpdateUI: (Song, Boolean) -> Unit
    )
    {

        currentSong = song
        uiUpdateCallback = onUpdateUI

        if (isManualClick) playedHistory.clear()
        playedHistory.add(song.id)

        notificationSync(currentSong,context)


        mediaPlayer?.release()
        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
        mediaPlayer = MediaPlayer.create(context, trackUri)
        mediaPlayer?.start()

        onUpdateUI(song, true)

        mediaPlayer?.setOnCompletionListener {
            playNext(context)
        }
    }

    fun togglePlayPause(context: Context){
        if(mediaPlayer?.isPlaying == true){
            mediaPlayer?.pause()
            uiUpdateCallback?.invoke(currentSong!!,false)
        }
        else{

            mediaPlayer?.start()
            uiUpdateCallback?.invoke(currentSong!!,true)
        }

        notificationSync(currentSong,context)
    }

    fun playNext(context: Context){
        if(currentSong == null) return

        val nextSong = when {
            isRadioMode && currentSongList.isNotEmpty() -> findNextRadioSong(currentSong!!,currentSongList)
            isAutoplay -> {
                val currentIndex = currentSongList.indexOf(currentSong)
                if(currentIndex != -1 && currentIndex < currentSongList.size - 1){
                    currentSongList[currentIndex + 1]
                } else null
            }
            else -> null
        }

        if(nextSong != null){
            uiUpdateCallback?.let { callback ->
                playTrack(context,nextSong,isManualClick = false, onUpdateUI = callback)
            }
        }
        else{
            uiUpdateCallback?.invoke(currentSong!!, true)
        }
    }

    fun playPrevious(context:Context){
        mediaPlayer?.seekTo(0)
        mediaPlayer?.start()
        uiUpdateCallback?.invoke(currentSong!!,true)
        notificationSync(currentSong,context)
    }

    fun stopTrack(){
        if(mediaPlayer?.isPlaying == true){
            mediaPlayer?.pause()
        }

        currentSong?.let { uiUpdateCallback?.invoke(it, false) }
    }

    fun seekTo(context:Context,position: Int){
        mediaPlayer?.seekTo(position)
        currentSong?.let {uiUpdateCallback?.invoke(it,mediaPlayer?.isPlaying == true)}
        notificationSync(currentSong,context)
    }
    private fun calculateDistance(vector1: String?, vector2: String?): Float{
       if(vector1 == null || vector2 == null)return Float.MAX_VALUE

        val v1 = vector1.split(",").mapNotNull {it.toFloatOrNull()}
        val v2 = vector2.split(",").mapNotNull {it.toFloatOrNull()}

        if(v1.size != v2.size || v1.isEmpty()) return Float.MAX_VALUE
        var sumOfSquares = 0.0f
        for(i in v1.indices){
            val diff = v1[i] - v2[i]
            sumOfSquares += diff*diff
        }
        return kotlin.math.sqrt(sumOfSquares)
    }

    private fun findNextRadioSong(currentSong:Song, songList:List<Song>): Song?{
        var availableSongs = songList.filter { it.id !in playedHistory }

        if(availableSongs.isEmpty()){
            playedHistory.clear()
            playedHistory.add(currentSong.id)

            availableSongs = songList.filter { it.id != currentSong.id }
        }


        return availableSongs.minByOrNull { targetSong ->
            calculateDistance(currentSong.acousticVector, targetSong.acousticVector)
        }

    }

    private fun notificationSync(currentSong: Song?,context: Context){
        currentSong?.let { song ->
            val serviceIntent = Intent(context, MusicService::class.java).apply {
                putExtra("SONG_TITLE", song.title)
                putExtra("SONG_ARTIST", song.artist)
                putExtra("SONG_DURATION", song.duration)
                putExtra("SONG_PATH",song.path)
            }
            context.startForegroundService(serviceIntent)
        }
    }
}

