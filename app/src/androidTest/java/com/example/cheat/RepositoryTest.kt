package com.example.cheat

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cheat.model.Message
import com.example.cheat.model.Repository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.util.*

@RunWith(AndroidJUnit4::class)
class RepositoryTest {
    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var repository: Repository

    @Before
    fun createDb() {
        val message1: Message = Message(0, "fill-up", Date.from(Instant.now()), false)
        val message2: Message = Message(1, "fill-ip", Date.from(Instant.now()), true)

        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = Repository(context)
        GlobalScope.launch {
            repository.insertMessage(message1)
            repository.insertMessage(message2)
        }

    }


    @Test
    @Throws(IOException::class)
    fun writeUserAndReadInList() {
        val message: Message = Message(2, "test", Date.from(Instant.now()), false)
        GlobalScope.launch {
            repository.insertMessage(message)
            val dbMessage = repository.getAllMessages().value!![0]
            assertTrue(message == dbMessage)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testGetMessageByText() {
        val message: Message = Message(2, "test-1", Date.from(Instant.now()), false)
        GlobalScope.launch {
            repository.insertMessage(message)
            val dbMessage = repository.getMessageByText("test-1")
            assertTrue(message == dbMessage)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testGetMessageByUUID() {
        val message: Message = Message(2, "test-1", Date.from(Instant.now()), false)
        GlobalScope.launch {
            repository.insertMessage(message)
            val dbMessage = repository.getMessageByUUID(2)
            assertTrue(message == dbMessage)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testUpdateMessage() {
        val message: Message = Message(2, "test-1", Date.from(Instant.now()), false)
        GlobalScope.launch {
            repository.insertMessage(message)
            val messageUp: Message = Message(2, "test-updated", Date.from(Instant.now()), false)
            repository.updateMessage(messageUp)
            val dbMessage = repository.getMessageByUUID(2)
            assertFalse(message == dbMessage)
            assertTrue(messageUp == dbMessage)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testDeleteMessage() {
        val message: Message = Message(2, "test-1", Date.from(Instant.now()), false)
        GlobalScope.launch {
            repository.insertMessage(message)
            repository.deleteMessage(message)
            val messages = repository.getAllMessages().value
            assertFalse(messages!!.contains(message))
        }
    }

    @Test
    @Throws(IOException::class)
    fun testDeleteAllMessages() {
        GlobalScope.launch {
            val dbMessage = repository.deleteAllMessage()
            val messages = repository.getAllMessages().value
            assertTrue(messages.isNullOrEmpty())
        }
    }
}