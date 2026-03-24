package com.brajo.localradio.ai

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.brajo.localradio.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YamnetWorker(
    appContext: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(appContext,workerParams)
{
    override suspend fun doWork(): Result{
        return withContext(Dispatchers.IO){

            try{
                val dao = AppDatabase.getDatabase(applicationContext).songDao()
                var processedCount = 0

                val yamnetAnalyzer = YamnetAnalyzer(context = applicationContext)

                try{
                    while (!isStopped){
                        val song = dao.getFirstUnanalyzedSong()

                        if(song == null){
                            println("YAMNet : Library fully analyzed, $processedCount song analyzed this run")
                            return@withContext Result.success()
                        }

//                        println("YAMNet : Analyzing [${song.title}]...")

                        val audioData = AudioExtractor.extractAudioChunkForAi(song.path)

                        if(audioData == null){
                            println("YAMNet Error: failed to read [${song.title}]")

                            dao.updateSong(song.copy(
                                isAnalyzed = true,
                                acousticVector = "ERROR_BAD_FILE"
                            ))
                            continue
                        }

                        val acousticVectorString = yamnetAnalyzer.getVibeVector(audioData)

                        if(acousticVectorString != null){

                            dao.updateSong(song.copy(
                                isAnalyzed = true,
                                acousticVector = acousticVectorString
                            ))
                            processedCount++

//                            println("YAMNet : Saved Vector for  ${song.title}..")
                        }else{
                            dao.updateSong(song.copy(
                                isAnalyzed = true,
                                acousticVector = "ERROR_AI_FAILED"
                            ))
                        }
                    }
                }
                catch(e:Exception){
                    e.printStackTrace()
                    println("YAMNET ERROR : error on Worker : ${e.message}")
                }

                finally {
                    yamnetAnalyzer.close()
                }

                println("YAMNet : Worker Processed $processedCount songs ")
                Result.success()
            }catch (e: Exception){
                e.printStackTrace()
                println("YAMNet Error : Worker Crashed!")
                Result.retry()
            }
        }
    }
}