package com.brajo.localradio

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>
}

@Database(entities = [Song::class], version = 2, exportSchema = false )
abstract class AppDatabase : RoomDatabase(){
    abstract fun songDao(): SongDao

}