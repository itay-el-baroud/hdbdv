package com.videoapi.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonElement
import com.videoapi.app.R
import com.videoapi.app.data.ApiService
import com.videoapi.app.data.local.AppDatabase
import com.videoapi.app.data.local.VideoEntity
import com.videoapi.app.ui.adapter.VideoAdapter
import com.videoapi.app.utils.Constants
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecordedFragment : Fragment() {

    private var adapter: VideoAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recorded, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerRecorded)
        val info = view.findViewById<TextView>(R.id.recordedInfo)

        adapter = VideoAdapter(emptyList()) { video ->
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("url", video.url)
            intent.putExtra("isLive", false)
            intent.putExtra("timestamp", video.timestamp)
            intent.putExtra("views", video.views)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val retrofit = Retrofit.Builder()
           .baseUrl(Constants.BASE_URL)
           .addConverterFactory(GsonConverterFactory.create())
           .build()
        val api = retrofit.create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                info.text = "جاري تحميل التسجيلات"
                val response = api.getOldList(1)
                val parsed = parseOldResponse(response)
                adapter?.update(parsed)
                info.text = "عدد التسجيلات ${parsed.size}"

                val dao = AppDatabase.getDatabase(requireContext()).videoDao()
                dao.insertAll(parsed)

            } catch (e: Exception) {
                info.text = "فشل التحميل، يتم عرض المخزن المحلي"
                try {
                    val dao = AppDatabase.getDatabase(requireContext()).videoDao()
                    val local = dao.getVideosByType(false)
                    adapter?.update(local)
                } catch (_: Exception) {}
            }
        }
    }

    private fun parseOldResponse(json: JsonElement): List<VideoEntity> {
        val list = mutableListOf<VideoEntity>()
        try {
            if (json.isJsonArray) {
                val arr = json.asJsonArray
                for (el in arr) {
                    if (!el.isJsonObject) continue
                    val o = el.asJsonObject
                    var url = ""
                    if (o.has("url")) url = o.get("url").asString
                    if (o.has("file")) url = o.get("file").asString
                    if (o.has("src")) url = o.get("src").asString
                    if (url.isEmpty()) continue
                    var title = "تسجيل"
                    if (o.has("title")) title = o.get("title").asString
                    if (o.has("name")) title = o.get("name").asString
                    var time = ""
                    if (o.has("time")) time = o.get("time").asString
                    if (o.has("date")) time = o.get("date").asString
                    var views = 0
                    if (o.has("views")) {
                        try { views = o.get("views").asInt } catch (_: Exception) {}
                    }
                    list.add(VideoEntity(
                        id = "old_${url.hashCode()}",
                        url = url,
                        title = title,
                        type = "recorded",
                        timestamp = time,
                        views = views,
                        isLive = false
                    ))
                }
            } else if (json.isJsonObject) {
                val obj = json.asJsonObject
                val arrayKeys = listOf("list", "data", "recordings", "items")
                for (key in arrayKeys) {
                    if (obj.has(key) && obj.get(key).isJsonArray) {
                        val arr = obj.getAsJsonArray(key)
                        for (el in arr) {
                            if (!el.isJsonObject) continue
                            val o = el.asJsonObject
                            var url = ""
                            if (o.has("url")) url = o.get("url").asString
                            if (o.has("file")) url = o.get("file").asString
                            if (o.has("src")) url = o.get("src").asString
                            if (url.isEmpty()) continue
                            var title = "تسجيل"
                            if (o.has("title")) title = o.get("title").asString
                            if (o.has("name")) title = o.get("name").asString
                            var time = ""
                            if (o.has("time")) time = o.get("time").asString
                            if (o.has("date")) time = o.get("date").asString
                            var views = 0
                            if (o.has("views")) {
                                try { views = o.get("views").asInt } catch (_: Exception) {}
                            }
                            list.add(VideoEntity(
                                id = "old_${url.hashCode()}",
                                url = url,
                                title = title,
                                type = "recorded",
                                timestamp = time,
                                views = views,
                                isLive = false
                            ))
                        }
                        break
                    }
                }
            }
        } catch (_: Exception) {}
        return list
    }
}
