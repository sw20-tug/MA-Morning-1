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

    @Query("DELETE FROM Message")
    fun deleteAllMessage()

    @Query("SELECT * FROM Message WHERE text = :sText")
    fun getMessageByText(sText: String): Message

    @Query("SELECT * FROM Message WHERE uid = :sUid")
    fun getMessageByUUID(sUid: Int): Message
}