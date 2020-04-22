package com.example.cheat

import android.content.Context
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
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = Repository(context)
    }


    @Test
    @Throws(IOException::class)
    fun writeUserAndReadInList() {
        val message: Message = Message(0, "test", Date.from(Instant.now()), false)
        GlobalScope.launch {
            repository.insertMessage(message)
            val dbMessage = repository.getAllMessages().value!![0]
            assertTrue(message == dbMessage)
        }
    }
}