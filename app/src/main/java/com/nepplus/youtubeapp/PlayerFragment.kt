package com.nepplus.youtubeapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.nepplus.youtubeapp.adapter.VideoAdapter
import com.nepplus.youtubeapp.databinding.FragmentPlayerBinding
import com.nepplus.youtubeapp.dto.VideoDto
import com.nepplus.youtubeapp.service.VideoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.abs

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var binding: FragmentPlayerBinding? = null
    private lateinit var videoAdapter: VideoAdapter
    private var player: ExoPlayer? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)

        binding = fragmentPlayerBinding
        initMotionLayoutEvent(fragmentPlayerBinding)
        initRecyclerView(fragmentPlayerBinding)
        initPlayer(fragmentPlayerBinding)
        initControlButton(fragmentPlayerBinding)
        initCancelButton(fragmentPlayerBinding)
        getVideoList()
    }

    private fun initMotionLayoutEvent(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playerMotionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
            }
            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
                binding?.let {
                    (activity as MainActivity).also { mainActivity ->
                        mainActivity.findViewById<MotionLayout>(R.id.mainMotionLayout).progress = abs(progress)
                    }
                }
            }
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
            }
            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {
            }

        })
    }

    private fun initRecyclerView(fragmentPlayerBinding: FragmentPlayerBinding) {
        videoAdapter = VideoAdapter(callback = { url, title ->
            play(url, title)
        }, context as MainActivity)
        fragmentPlayerBinding.fragmentRecyclerView.apply {
            adapter = videoAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initPlayer(fragmentPlayerBinding: FragmentPlayerBinding) {
        context?.let {
            player = ExoPlayer.Builder(it).build()
        }

        fragmentPlayerBinding.playerView.player = player
        binding?.let {
            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying) {
                        it.bottomPlayerControlButton.setImageResource(R.drawable.ic_baseline_pause_24)

                    } else {
                        it.bottomPlayerControlButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                    it.bottomDeleteButton.setImageResource(R.drawable.ic_baseline_close_24)
                }
            })
        }
    }

    private fun initControlButton(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.bottomPlayerControlButton.setOnClickListener {
            val player = this.player ?: return@setOnClickListener

            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    private fun initCancelButton(fragmentPlayerBinding: FragmentPlayerBinding){
        fragmentPlayerBinding.bottomDeleteButton.setOnClickListener {
            player?.stop()
            (context as MainActivity).findViewById<FrameLayout>(R.id.fragmentContainer).visibility = View.GONE
        }
    }

    private fun getVideoList() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(VideoService::class.java).also {
            it.listVideo()
                .enqueue(object : Callback<VideoDto> {
                    override fun onResponse(call: Call<VideoDto>, response: Response<VideoDto>) {
                        if (response.isSuccessful.not()) {
                            Log.d("MainActivity", "response fail")
                            return
                        }
                        response.body()?.let { videoDto ->
                            videoAdapter.submitList(videoDto.videos)
                        }
                    }

                    override fun onFailure(call: Call<VideoDto>, t: Throwable) {
                    }

                })
        }
    }

    fun play(url: String, title: String) {

        context?.let {
            //data source ??? ???????????? media source ??? ??????
            val dataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
            player?.setMediaSource(mediaSource)
            player?.prepare()
            player?.play()
        }

        binding?.let {

            it.playerMotionLayout.transitionToEnd()
            it.bottomTitleTextView.text = title
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
        player?.release()
    }

}