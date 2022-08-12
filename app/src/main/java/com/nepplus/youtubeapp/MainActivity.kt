package com.nepplus.youtubeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nepplus.youtubeapp.adapter.VideoAdapter
import com.nepplus.youtubeapp.databinding.ActivityMainBinding
import com.nepplus.youtubeapp.dto.VideoDto
import com.nepplus.youtubeapp.service.VideoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var videoAdapter : VideoAdapter


    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlayerFragment())
            .commit()


        videoAdapter = VideoAdapter(callback = {url, title ->
            //이액티비티에 attach 되어있는 fragment 를 전부가져온 뒤 PlayerFragment 형의 첫번쨰가 it
            supportFragmentManager.fragments.find { it is PlayerFragment }?.let {
                (it as PlayerFragment).play(url,title)
            }
        }, this)

        findViewById<RecyclerView>(R.id.mainRecyclerView).apply {
            adapter = videoAdapter
            layoutManager = LinearLayoutManager(context)
        }

//        binding.mainBottomNavigationView.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.home ->  binding.fragmentContainer.visibility= View.GONE
//            }
//            return@setOnItemSelectedListener true
//        }

//        binding.fragmentContainer.visibility= View.GONE


        // Toolbar 추가
        setSupportActionBar(binding.mainToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)  // 왼쪽 버튼 사용 여부 true
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.resize_youtube)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        getVideoList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var myIntent: Intent
        when(item.itemId){
            R.id.searchBtn -> {
//                myIntent = Intent(mContext, SearchActivity::class.java)
//                startActivity(myIntent)
            }
            else ->{
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_nav_menu, menu)
        return true
    }

    private fun getVideoList() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(VideoService::class.java).also {
            it.listVideo()
                .enqueue(object : Callback<VideoDto>{
                    override fun onResponse(call: Call<VideoDto>, response: Response<VideoDto>) {
                        if(response.isSuccessful.not()){
                            Log.d("MainActivity", "response fail")
                            return
                        }
                        response.body()?.let{ videoDto ->
                            videoAdapter.submitList(videoDto.videos)
                        }
                    }

                    override fun onFailure(call: Call<VideoDto>, t: Throwable) {
                    }

                })
        }
    }
}