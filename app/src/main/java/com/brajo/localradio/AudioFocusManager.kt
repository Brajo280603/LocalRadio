package com.brajo.localradio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager


class AudioFocusManager(
    context: Context,
    private val isPlaying: () -> Boolean,
    private val onPauseRequired: () -> Unit,
    private val onResumeRequired: () -> Unit,
    private val onDuckRequired: () -> Unit,
    private val onRestoreVolumeRequired: () -> Unit
): AudioManager.OnAudioFocusChangeListener {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private var resumeOnFocusGain = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this)
            .build()
    }

    fun requestAudioFocus(): Boolean{
        resumeOnFocusGain = false

        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun abandonAudioFocus(){
        resumeOnFocusGain = false
        audioFocusRequest?.let {audioManager.abandonAudioFocusRequest(it)}
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnFocusGain = false
                onPauseRequired()
                abandonAudioFocus()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if(isPlaying() || resumeOnFocusGain){
                    resumeOnFocusGain = true
                    onPauseRequired()
                }else{
                    resumeOnFocusGain = false
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                onDuckRequired()
            }

            AudioManager.AUDIOFOCUS_GAIN ->{
                val isInCall = audioManager.mode == AudioManager.MODE_IN_CALL ||
                        audioManager.mode == AudioManager.MODE_IN_COMMUNICATION

                if(resumeOnFocusGain || !isInCall){
                    resumeOnFocusGain = false
                    onRestoreVolumeRequired()
                    onResumeRequired()
                }
            }
        }
    }


}