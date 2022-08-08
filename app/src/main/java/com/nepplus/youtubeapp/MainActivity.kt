package com.nepplus.youtubeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.nepplus.youtubeapp.dto.VideoDto
import com.nepplus.youtubeapp.service.VideoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlayerFragment())
            .commit()

        getVideoList()
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
                        response.body()?.let{
                            Log.d("MainActivity", it.toString())
                        }
                    }

                    override fun onFailure(call: Call<VideoDto>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }
}