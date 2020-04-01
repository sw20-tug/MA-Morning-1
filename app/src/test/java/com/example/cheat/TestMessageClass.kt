package com.example.cheat

import com.example.cheat.model.Message
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

class TestMessageClass {
    @Test
    fun createMessage(){
        val msg = "test"
        val date = Date.from(Instant.now())

        var obj = Message(0, msg, date, false)

        assertEquals(obj.text, msg)
        assertEquals(obj.date, date)
    }

}