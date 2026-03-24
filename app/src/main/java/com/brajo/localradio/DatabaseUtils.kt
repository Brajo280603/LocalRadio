package com.brajo.localradio

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object DatabaseUtils {
    suspend fun exportDatabase(context: Context, targetUri: Uri): Boolean {
        return withContext(Dispatchers.IO){
            try{
                val db = AppDatabase.getDatabase(context)
                db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").moveToFirst()

                val dbFile = context.getDatabasePath("local-radio-db")
                if(!dbFile.exists()){
                    println("DB_EXPORT Error : cant find database ")
                    return@withContext false
                }

                context.contentResolver.openOutputStream(targetUri)?.use{ outputStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                println("DB_EXPORT : exported Successfully")
                true
            }catch (e: Exception){
                e.printStackTrace()
                println("DB_EXPORT Error: ${e.message}")
                false
            }
        }
    }

    suspend fun importDatabase(context: Context, sourceUri: Uri): Boolean {
        return withContext(Dispatchers.IO){
            try{
                val db = AppDatabase.getDatabase(context)
                if(db.isOpen){
                    db.close()
                }

                val dbName = "local-radio-db"
                val dbFile = context.getDatabasePath(dbName)

                val walFile = File(dbFile.path + "-wal")
                val shmFile = File(dbFile.path + "-shm")

                if(dbFile.exists()) dbFile.delete()
                if(walFile.exists()) walFile.delete()
                if(shmFile.exists()) shmFile.delete()

                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                println("DB_IMPORT : import done successfully!")
                true
            }catch (e: Exception){
                e.printStackTrace()
                println("DB_IMPORT Error : ${e.message}")
                false
            }

        }
    }
}