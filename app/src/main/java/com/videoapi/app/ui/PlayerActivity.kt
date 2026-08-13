package com.videoapi.app.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.videoapi.app.R

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val url = intent.getStringExtra("url")?: ""
        val isLive = intent.getBooleanExtra("isLive", false)
        val timestamp = intent.getStringExtra("timestamp")?: ""
        val views = intent.getIntExtra("views", 0)

        val playerView = findViewById<PlayerView>(R.id.playerView)
        val statusText = findViewById<TextView>(R.id.playerStatus)
        val timeText = findViewById<TextView>(R.id.playerTime)
        val viewsText = findViewById<TextView>(R.id.playerViews)

        if (isLive) {
            statusText.text = "live feed / بث مباشر"
            statusText.setBackgroundColor(getColor(R.color.live_red))
        } else {
            statusText.text = "Recorded broadcast / بث مسجل"
            statusText.setBackgroundColor(getColor(R.color.recorded_gray))
        }
        timeText.text = "تم ارسال هذا البث في توقيت $timestamp"
        viewsText.text = "عدد المشاهدات $views"

        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        playerView.setShowNextButton(true)
        playerView.setShowPreviousButton(true)

        val mediaItem = MediaItem.fromUri(url)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()

        playerView.findViewById<View>(R.id.exo_fullscreen)?.setOnClickListener {
            toggleFullscreen()
        }
    }

    private fun toggleFullscreen() {
        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            isFullscreen = false
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isFullscreen = true
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
