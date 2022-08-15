package com.nepplus.youtubeapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class History (
    @PrimaryKey val uid : Int?,
    @ColumnInfo(name = "search")val search : String? = null
    )
