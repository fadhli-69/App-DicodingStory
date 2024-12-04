package com.example.dicodingstory.data.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class AddStoryResponse(
    val error: Boolean,
    val message: String
)

data class GetAllStoriesResponse(
    val error: Boolean,
    val message: String,
    val listStory: List<Story>
)

@Parcelize
data class Story(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: String,
    val lat: Double?,
    val lon: Double?
) : Parcelable

