package com.aqtanb.tronchecker.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val address: String,
    val timestamp: Long = System.currentTimeMillis()
)