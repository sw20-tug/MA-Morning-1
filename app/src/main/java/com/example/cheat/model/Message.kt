package com.example.cheat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.*

@Entity
class Message(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "text") val text: String?,
    @ColumnInfo(name = "date") val date: Date?,
    @ColumnInfo(name = "belongs_to_current") val belongsToCurrentUser: Boolean){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (uid != other.uid) return false
        if (text != other.text) return false
        if (date != other.date) return false
        if (belongsToCurrentUser != other.belongsToCurrentUser) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + belongsToCurrentUser.hashCode()
        return result
    }
}