package com.example.cheat.model

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface MessageDao {
    @Query("SELECT * FROM Message")
    fun loadAllMessages(): LiveData<List<Message>>

    @Insert
    fun insertAll(vararg messages: Message)

    @Update
    fun updateMessages(vararg messages: Message)

    @Delete
    fun delete(msg: Message)
}