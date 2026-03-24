package com.brajo.localradio

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(songs: List<Song>)

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isAnalyzed = 0 LIMIT 1")
    suspend fun getFirstUnanalyzedSong(): Song?

    @Update
    suspend fun updateSong(song: Song)

    @Query("SELECT COUNT(*) FROM songs WHERE isAnalyzed = 1")
    fun getAnalyzedSongsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM songs")
    fun getTotalSongsCount(): Flow<Int>
}

@Database(entities = [Song::class], version = 3, exportSchema = false )
abstract class AppDatabase : RoomDatabase(){
    abstract fun songDao(): SongDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "local-radio-db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

}