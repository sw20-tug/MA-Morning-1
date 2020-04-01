package com.example.cheat.model

import androidx.room.*


@Dao
interface MessageDao {
    @Query("SELECT * FROM Message")
    fun loadAllMessages(): Array<Message>

    @Insert
    fun insertAll(vararg messages: Message)

    @Update
    fun updateMessages(vararg messages: Message)

    @Delete
    fun delete(msg: Message)
}