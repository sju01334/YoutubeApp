package com.nepplus.youtubeapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nepplus.youtubeapp.databinding.ItemSearchBinding
import com.nepplus.youtubeapp.model.History

class HistoryAdapter(
    val historyDeleteClickedListener : (String) -> Unit
): ListAdapter<History, HistoryAdapter.ViewHolder>(diffUtil){
    inner class  ViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item : History){
            binding.historySearchTxt.text = item.keyword


            binding.historyDeleteButton.setOnClickListener {
                historyDeleteClickedListener(item.keyword.orEmpty())
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(currentList[position])
    }

    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<History>(){
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem.keyword == newItem.keyword
            }

        }
    }



}