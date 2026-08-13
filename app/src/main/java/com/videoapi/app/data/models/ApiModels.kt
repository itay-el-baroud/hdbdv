package com.videoapi.app.data.models

import com.google.gson.annotations.SerializedName

data class LiveSeg(
    @SerializedName("url") val url: String? = null,
    @SerializedName("src") val src: String? = null,
    @SerializedName("file") val file: String? = null,
    @SerializedName("seq") val seq: Long? = null,
    @SerializedName("ts") val ts: Long? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("views") val views: Int? = null
)

data class LiveResponseParsed(
    val seq: Long,
    val segments: List<LiveSeg>
)

data class OldItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("file") val file: String? = null,
    @SerializedName("src") val src: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("views") val views: Int? = null,
    @SerializedName("duration") val duration: String? = null
)
