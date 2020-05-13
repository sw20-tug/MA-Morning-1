package com.example.cheat.model

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class Repository(context: Context) {
    private var messageDao: MessageDao = AppDatabase.getInstance(context).getMessageDao()

    fun getAllMessages(): LiveData<List<Message>> {
        return messageDao.loadAllMessages()
    }

    suspend fun insertMessage(message: Message) {
        insertMessageAsync(message)
    }

    suspend fun updateMessage(message: Message) {
        updateMessageAsync(message)
    }

    suspend fun deleteMessage(message: Message) {
        deleteMessageAsync(message)
    }

    suspend fun deleteAllMessage() {
        deleteAllMessageAsync()
    }

    private suspend fun insertMessageAsync(message: Message) {
        withContext(Dispatchers.IO) {
            messageDao.insertAll(message)
        }
    }

    private suspend fun updateMessageAsync(message: Message) {
        withContext(Dispatchers.IO) {
            messageDao.updateMessages(message)
        }
    }

    private suspend fun deleteMessageAsync(message: Message) {
        withContext(Dispatchers.IO) {
            messageDao.delete(message)
        }
    }

    private suspend fun deleteAllMessageAsync() {
        withContext(Dispatchers.IO) {
            messageDao.deleteAllMessage()
        }
    }
}