package com.videoapi.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val type: String,
    val timestamp: String,
    val views: Int,
    val isLive: Boolean,
    val localPath: String? = null
)
