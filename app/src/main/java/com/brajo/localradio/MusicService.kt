package com.brajo.localradio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

class MusicService : Service() {

    private lateinit var mediaSession: MediaSessionCompat

    private var currentTitle: String = "Unknown Title"
    private var currentArtist: String = "Unknown Artist"
    private var currentDuration: Long = 0L
    private var currentPath: String = ""
    private var isClassicRadioMode : Boolean = false

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(this,"LocalRadioSession")
        mediaSession.isActive = true

        mediaSession.setCallback(object : MediaSessionCompat.Callback(){
            override fun onPlay() {
                PlaybackManager.togglePlayPause(this@MusicService)
            }
            override fun onPause() {
                PlaybackManager.togglePlayPause(this@MusicService)
            }
            override fun onSkipToNext() {
                PlaybackManager.playNext(this@MusicService)
            }
            override fun onSkipToPrevious() {
                PlaybackManager.playPrevious(this@MusicService)
            }
            override fun onStop() {
                PlaybackManager.stopTrack()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            override fun onSeekTo(pos: Long) {
                PlaybackManager.seekTo(this@MusicService,pos.toInt())
                val intent = Intent(this@MusicService, MusicService::class.java)
                startService(intent)
            }


        }
        )
    }

    override fun onBind(intent: Intent?): IBinder?{
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){
            "ACTION_PAUSE" -> PlaybackManager.togglePlayPause(this)
            "ACTION_NEXT" -> PlaybackManager.playNext(this)
            "ACTION_PREV" -> PlaybackManager.playPrevious(this)
            "ACTION_STOP" -> {
                PlaybackManager.stopTrack()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        intent?.getStringExtra("SONG_TITLE")?.let { currentTitle = it }
        intent?.getStringExtra("SONG_ARTIST")?.let { currentArtist = it }
        intent?.getLongExtra("SONG_DURATION",0L)?.let { if(it > 0L) currentDuration = it }
        intent?.getStringExtra("SONG_PATH")?.let {currentPath = it}
        intent?.getBooleanExtra("IS_CLASSIC_MODE",false)?.let{isClassicRadioMode = it}

        val albumArtBitmap = getAlbumArt(currentPath)

        val channelId = "LocalRadioChannel"
        val channel = NotificationChannel(channelId, "Music Playback", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val pauseIntent = PendingIntent.getService(this,1,Intent(this,MusicService::class.java).setAction("ACTION_PAUSE"), PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = PendingIntent.getService(this,2,Intent(this,MusicService::class.java).setAction("ACTION_NEXT"), PendingIntent.FLAG_IMMUTABLE)
        val prevIntent = PendingIntent.getService(this,3,Intent(this,MusicService::class.java).setAction("ACTION_PREV"), PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = PendingIntent.getService(this,4,Intent(this, MusicService::class.java).setAction("ACTION_STOP"), PendingIntent.FLAG_IMMUTABLE)

        val metadataBuilder = android.support.v4.media.MediaMetadataCompat.Builder()
            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE,currentTitle)
            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST,currentArtist)
            .putLong(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION,currentDuration)

        if(albumArtBitmap != null){
            metadataBuilder.putBitmap(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ART,albumArtBitmap)
            metadataBuilder.putBitmap(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART,albumArtBitmap)
        }

        mediaSession.setMetadata(metadataBuilder.build())


        val isPlaying = PlaybackManager.mediaPlayer?.isPlaying == true
        val playPauseIcon = if(isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseText = if(isPlaying) "Pause" else "Play"

        val state = if(isPlaying) android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
                    else android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED

        val playbackBuilder = android.support.v4.media.session.PlaybackStateCompat.Builder()
            .setState(state, PlaybackManager.mediaPlayer?.currentPosition?.toLong() ?:0L, 1.0f)
        if(!isClassicRadioMode){
            playbackBuilder.setActions(
                android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE or
                android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                android.support.v4.media.session.PlaybackStateCompat.ACTION_SEEK_TO
            )
        }else{
            playbackBuilder.setActions(
                android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
        }

        mediaSession.setPlaybackState(playbackBuilder.build())

        val notificationBuilder = NotificationCompat.Builder(this,channelId)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)

            .addAction(playPauseIcon,playPauseText,pauseIntent)

            .setLargeIcon(albumArtBitmap)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0,1,2)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setDeleteIntent(stopIntent)
            .setOngoing(isPlaying)

        if(!isClassicRadioMode){
            notificationBuilder.addAction(android.R.drawable.ic_media_previous,"previous",prevIntent)
                .addAction(android.R.drawable.ic_media_next,"next",nextIntent)
        }



        val notification = notificationBuilder.build()

        startForeground(1,notification)

        manager.notify(1,notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaSession.release()
    }

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