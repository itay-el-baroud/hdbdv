package com.videoapi.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.videoapi.app.R
import com.videoapi.app.data.local.AppDatabase
import com.videoapi.app.data.local.VideoEntity
import com.videoapi.app.ui.adapter.VideoAdapter
import com.videoapi.app.utils.Constants
import com.videoapi.app.utils.DeviceIdUtil
import com.videoapi.app.data.ApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LiveFragment : Fragment() {

    private var lastSeqCam: Long = -1
    private var lastSeqScr: Long = -1
    private val handler = Handler(Looper.getMainLooper())
    private var adapter: VideoAdapter? = null
    private val allVideos = mutableListOf<VideoEntity>()
    private var api: ApiService? = null
    private var runnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_live, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofit = Retrofit.Builder()
           .baseUrl(Constants.BASE_URL)
           .addConverterFactory(GsonConverterFactory.create())
           .build()
        api = retrofit.create(ApiService::class.java)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerLive)
        val statusInfo = view.findViewById<TextView>(R.id.liveStatusInfo)
        val btnCam = view.findViewById<Button>(R.id.btnCam)
        val btnScr = view.findViewById<Button>(R.id.btnScr)

        adapter = VideoAdapter(allVideos) { video ->
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("url", video.url)
            intent.putExtra("isLive", video.isLive)
            intent.putExtra("timestamp", video.timestamp)
            intent.putExtra("views", video.views)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        var currentChannel = Constants.LIVE_CAM

        btnCam.setOnClickListener {
            currentChannel = Constants.LIVE_CAM
            statusInfo.text = "يعرض بث الكاميرا"
        }
        btnScr.setOnClickListener {
            currentChannel = Constants.LIVE_SCREEN
            statusInfo.text = "يعرض بث الشاشة"
        }

        runnable = object : Runnable {
            override fun run() {
                fetchLive(currentChannel)
                handler.postDelayed(this, Constants.POLL_INTERVAL_MS)
            }
        }
        handler.post(runnable as Runnable)
    }

    private fun fetchLive(channel: String) {
        val deviceId = DeviceIdUtil.getDeviceId(requireContext())
        val after = if (channel == Constants.LIVE_CAM) lastSeqCam else lastSeqScr

        lifecycleScope.launch {
            try {
                val apiService = api?: return@launch
                val response = apiService.getLive(deviceId, channel, after)
                parseLiveResponse(response, channel)
            } catch (e: Exception) {
                // ignore for polling
            }
        }
    }

    private fun parseLiveResponse(json: JsonElement, channel: String) {
        try {
            val obj = json.asJsonObject
            var seq = -1L
            if (obj.has("seq")) seq = obj.get("seq").asLong
            if (obj.has("last_seq")) seq = obj.get("last_seq").asLong

            val segs = mutableListOf<VideoEntity>()
            if (obj.has("segs") && obj.get("segs").isJsonArray) {
                val arr = obj.getAsJsonArray("segs")
                for (el in arr) {
                    if (!el.isJsonObject) continue
                    val o = el.asJsonObject
                    var url = ""
                    if (o.has("url")) url = o.get("url").asString
                    if (o.has("src")) url = o.get("src").asString
                    if (o.has("file")) url = o.get("file").asString
                    if (url.isEmpty()) continue
                    var v = 0
                    if (o.has("views")) {
                        try { v = o.get("views").asInt } catch (_: Exception) {}
                    }
                    var t = System.currentTimeMillis().toString()
                    if (o.has("time")) t = o.get("time").asString
                    if (o.has("ts")) t = o.get("ts").asString
                    val entity = VideoEntity(
                        id = "${channel}_${seq}_${url.hashCode()}",
                        url = url,
                        title = "بث $channel",
                        type = channel,
                        timestamp = t,
                        views = v,
                        isLive = true
                    )
                    segs.add(entity)
                }
            }
            if (segs.isNotEmpty()) {
                if (channel == Constants.LIVE_CAM) lastSeqCam = seq else lastSeqScr = seq
                allVideos.addAll(0, segs)
                adapter?.update(allVideos.toList())
                // save to db
                lifecycleScope.launch {
                    val dao = AppDatabase.getDatabase(requireContext()).videoDao()
                    dao.insertAll(segs)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        runnable?.let { handler.removeCallbacks(it) }
    }
}
