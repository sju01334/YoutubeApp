package com.nepplus.youtubeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.nepplus.youtubeapp.adapter.HistoryAdapter
import com.nepplus.youtubeapp.adapter.VideoAdapter
import com.nepplus.youtubeapp.databinding.ActivityMainBinding
import com.nepplus.youtubeapp.dto.VideoDto
import com.nepplus.youtubeapp.model.History
import com.nepplus.youtubeapp.service.VideoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var videoAdapter : VideoAdapter
    private lateinit var historyAdapter : HistoryAdapter
    private lateinit var binding : ActivityMainBinding

    private lateinit var retrofit : Retrofit

    private lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "db"
        ).build()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlayerFragment())
            .commit()


        videoAdapter = VideoAdapter(callback = {url, title ->
            //이액티비티에 attach 되어있는 fragment 를 전부가져온 뒤 PlayerFragment 형의 첫번쨰가 it
            supportFragmentManager.fragments.find { it is PlayerFragment }?.let {
                (it as PlayerFragment).play(url,title)
            }
        }, this)

        binding.mainRecyclerView.apply {
            adapter = videoAdapter
            layoutManager = LinearLayoutManager(context)
        }

        historyAdapter = HistoryAdapter(historyDeleteClickedListener = {
            deleteSearch(it)
        })

        binding.searchRecyclerView.apply {
            adapter = historyAdapter
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


        binding.searchEdt.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                Toast.makeText(this,binding.searchEdt.text.toString() , Toast.LENGTH_SHORT).show()
                //Room 에 Data 넣기
                addRecentSearch(binding.searchEdt.text.toString())
                //search item 골라서 가져오기
                searchList(binding.searchEdt.text.toString())
                downKeypad()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.searchEdt.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                showHistoryView()
            }
            return@setOnTouchListener false
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var myIntent: Intent
        when(item.itemId){
            R.id.searchBtn -> {
                showHistoryView()
            }
            android.R.id.home -> {
                backbuttonClicked()
                downKeypad()
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


    private fun addRecentSearch(keyword : String){
        Thread{
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    private  fun deleteSearch(keyword : String){
        Thread{
            db.historyDao().delete(keyword)
            showHistoryView()
        }.start()

    }

    private fun showHistoryView(){
        supportActionBar!!.setHomeAsUpIndicator(null)
        binding.searchEdt.isVisible = true
        binding.mainRecyclerView.isVisible = false

        Thread {
            val keywords = db.historyDao().getAll().reversed()
            Log.d("this", keywords.toString())
            runOnUiThread {
                historyAdapter.submitList(keywords.orEmpty())
                historyAdapter.notifyDataSetChanged()
                binding.searchRecyclerView.isVisible = true
            }
        }.start()

        binding.searchRecyclerView.isVisible = true
    }

    private fun searchList(keyword : String){
        binding.mainRecyclerView.isVisible = true
        binding.searchRecyclerView.isVisible = false

        retrofit.create(VideoService::class.java).also {
            it.listVideo()
                .enqueue(object : Callback<VideoDto>{
                    override fun onResponse(call: Call<VideoDto>, response: Response<VideoDto>) {
                        if(response.isSuccessful.not()){
                            Log.d("MainActivity", "response fail")
                            return
                        }
                        response.body()?.let{ videoDto ->
                            videoAdapter.submitList(videoDto.videos.filter { it.title == keyword })
                        }
                    }
                    override fun onFailure(call: Call<VideoDto>, t: Throwable) {
                    }
                })
        }

    }

    private fun backbuttonClicked(){
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.resize_youtube)
        binding.searchEdt.isVisible = false
        binding.mainRecyclerView.isVisible = true
        binding.searchRecyclerView.isVisible = false
        getVideoList()
    }


    private fun getVideoList() {

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

    private fun downKeypad(){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEdt.windowToken, 0)
    }
}