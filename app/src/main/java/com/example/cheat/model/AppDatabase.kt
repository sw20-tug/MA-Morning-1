package com.example.cheat.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(Message::class), version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMessageDao(): MessageDao
}