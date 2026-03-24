package com.brajo.localradio.ai

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class YamnetAnalyzer(context: Context){
    private var interpreter: Interpreter? = null
    private var nnApiDelegate: NnApiDelegate? = null

    init {
        try{
            val modelBuffer = loadModelFile(context,"yamnet.tflite")

//            nnApiDelegate = NnApiDelegate()

            val options = Interpreter.Options().apply{
//                addDelegate(nnApiDelegate)
                numThreads = 4
            }

            interpreter = Interpreter(modelBuffer,options)

            println("YAMNet loaded successfully!")
        }catch (e: Exception){
            e.printStackTrace()
            println("Error in YAMNet : ${e.message}")
        }
    }

    private fun loadModelFile(context: Context,modelName: String): MappedByteBuffer{
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun getVibeVector(audioData: FloatArray): String?{
        val tflite = interpreter?: return null

        try{

            val outputCount = interpreter!!.outputTensorCount
            val isEmbedding = outputCount == 3
            val vectorSize = if(isEmbedding) 1024 else 521
            val finalVector = FloatArray(vectorSize)

            val isInput2D = tflite.getInputTensor(0).shape().size == 2
            val isOutput2D = tflite.getOutputTensor(0).shape().size == 2

            var chunksProcessed = 0
            val chunkSize = 15600

            for(i in 0 until audioData.size - chunkSize step chunkSize){
                val inputBuffer = audioData.copyOfRange(i, i + chunkSize)
                val formattedInput = if(isInput2D) arrayOf(inputBuffer) else inputBuffer

                val outputMap = mutableMapOf<Int, Any>()
                val chunkVector : FloatArray

                if(isEmbedding){
                    val embeddingOutput = Array(1) { FloatArray(1024) }
                    outputMap[0] = Array(1) { FloatArray(521) }
                    outputMap[1] = embeddingOutput
                    outputMap[2] =  Array(96) { FloatArray(64)}

                    tflite.runForMultipleInputsOutputs(arrayOf(formattedInput),outputMap)

                    chunkVector = embeddingOutput[0]
                }else{
                    if(isOutput2D){
                        val scoresOutput = Array(1) { FloatArray(521) }
                        outputMap[0] = scoresOutput
                        tflite.runForMultipleInputsOutputs(arrayOf(formattedInput),outputMap)
                        chunkVector = scoresOutput[0]
                    }else{
                        val scoresOutput = FloatArray(521)
                        outputMap[0] = scoresOutput
                        tflite.runForMultipleInputsOutputs(arrayOf(formattedInput),outputMap)
                        chunkVector = scoresOutput
                    }
                }

                for (j in 0 until vectorSize){
                    finalVector[j] += chunkVector[j]
                }
                chunksProcessed++
            }

            if(chunksProcessed > 0){
                for(j in 0 until vectorSize){
                    finalVector[j] = finalVector[j]/ chunksProcessed
                }
            }else{
                return null
            }

            return finalVector.joinToString(",")
        } catch (e: Exception){
            e.printStackTrace()
            println("YAMNet Error : ${e.message}")
            return null
        }
    }

    fun close(){
        interpreter?.close()
        interpreter = null

        nnApiDelegate?.close()
        nnApiDelegate = null
    }
}