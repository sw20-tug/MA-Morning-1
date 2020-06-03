package com.example.cheat

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.cheat.model.Message
import com.example.cheat.model.Repository
import kotlinx.coroutines.launch

class MessageViewModel(application: Application): AndroidViewModel(application) {
    private var repository: Repository = Repository(application)

    fun getAllMessages(): LiveData<List<Message>> {
        return repository.getAllMessages()
    }

    fun getMessageByText(text: String): Message {
        return repository.getMessageByText(text)
    }

    fun getMessageByUUID(uuid: Int): Message {
        return repository.getMessageByUUID(uuid)
    }

    fun insertMessage(message: Message) {
        viewModelScope.launch { repository.insertMessage(message) }
    }

    fun updateMessage(message: Message) {
        viewModelScope.launch { repository.updateMessage(message) }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch { repository.deleteMessage(message) }
    }

    fun deleteAllMessage() {
        viewModelScope.launch { repository.deleteAllMessage() }
    }
}