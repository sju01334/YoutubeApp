package com.nepplus.youtubeapp.service

import com.nepplus.youtubeapp.dto.VideoDto
import retrofit2.Call
import retrofit2.http.GET

interface VideoService {

    @GET("v3/798f4703-ad72-4182-b91c-10d4f78fc2f7")
    fun listVideo() : Call<VideoDto>
}