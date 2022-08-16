package com.nepplus.youtubeapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nepplus.youtubeapp.dao.HistoryDao
import com.nepplus.youtubeapp.model.History

@Database(entities = [History::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun historyDao() : HistoryDao

}