package com.brajo.localradio.ai

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioExtractor{
    private const val TARGET_SAMPLE_RATE = 16000
    private const val DURATION_SEC = 10
    private const val TARGET_FLOAT_COUNT = TARGET_SAMPLE_RATE * DURATION_SEC
    private const val TIMEOUT_US = 10000L

    fun extractAudioChunkForAi(songPath: String): FloatArray?{
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        return try {
            extractor.setDataSource(songPath)
            val format = selectAudioTrack(extractor)?: return null
            val originalSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null

            val durationUS = try{
                format.getLong(MediaFormat.KEY_DURATION)
            } catch(e: Exception){
                0L
            }

            if(durationUS > 30_000_000){
                extractor.seekTo(30_000_000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            }else{
                extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            }

            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format,null,null,0)
            codec.start()

            decodeAudio(extractor,codec, channels, originalSampleRate)
        }catch(e: Exception){
            e.printStackTrace()
            null
        }finally {
            codec?.stop()
            codec?.release()
            extractor.release()
        }

    }

    private fun selectAudioTrack(extractor: MediaExtractor): MediaFormat?{
        for(i in 0 until extractor.trackCount){
            val format = extractor.getTrackFormat(i)
            if(format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true){
                extractor.selectTrack(i)
                return format
            }
        }
        return null
    }

    private fun decodeAudio(
        extractor: MediaExtractor,
        codec: MediaCodec,
        channels: Int,
        originalSampleRate: Int
    ): FloatArray? {
        val floatList = mutableListOf<Float>()
        val info = MediaCodec.BufferInfo()
        var isEOS = false

        while (floatList.size < TARGET_FLOAT_COUNT){
            if(!isEOS){
                val inIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                if(inIndex >= 0){
                    val buffer = codec.getInputBuffer(inIndex)
                    val sampleSize = if(buffer != null) extractor.readSampleData(buffer,0) else -1
                    if(sampleSize < 0){
                        codec.queueInputBuffer(inIndex,0,0,0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isEOS = true
                    }else{
                        codec.queueInputBuffer(inIndex,0,sampleSize,extractor.sampleTime,0)
                        extractor.advance()
                    }
                }
            }

            val outIndex = codec.dequeueOutputBuffer(info,TIMEOUT_US)
            if(outIndex >= 0){
                val outBuffer = codec.getOutputBuffer(outIndex)
                if(outBuffer != null && info.size > 0){
                    outBuffer.position(info.offset)
                    outBuffer.limit(info.offset + info.size)

                    processRawAudioMath(outBuffer,channels,originalSampleRate, floatList)
                }
                codec.releaseOutputBuffer(outIndex,false)
                if((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
            }
        }

        val finalArray = floatList.toFloatArray()

        if(finalArray.isEmpty()) {
            return null
        }
        return finalArray

    }



    private  fun processRawAudioMath(
        buffer: ByteBuffer,
        channels: Int,
        originalSampleRate: Int,
        floatList: MutableList<Float>
    ){

        //16-bit PCM to 32-bit Float(-1.0 to 1.0)
        val shortBuffer = buffer.order(ByteOrder.nativeOrder()).asShortBuffer()
        val floatArrayOutput = FloatArray(shortBuffer.remaining())
        for(i in 0 until shortBuffer.remaining()){
            floatArrayOutput[i] = shortBuffer.get(i) / 32768f
        }

        //converting multi-channels to Mono
        val monoChunk = if(channels > 1){
            val mono = FloatArray(floatArrayOutput.size/channels)

            for (i in mono.indices){
                var sum = 0f
                for(c in 0 until channels){
                    sum += floatArrayOutput[i * channels + c]
                }
                mono[i] = sum/ channels
            }

            mono
        }else{
            floatArrayOutput
        }

        //Downsample to 16khz
        val ratio = originalSampleRate.toDouble() / TARGET_SAMPLE_RATE.toDouble()
        var j = 0.0
        while(j < monoChunk.size && floatList.size < TARGET_FLOAT_COUNT){
            floatList.add(monoChunk[j.toInt()])
            j += ratio
        }
    }
}