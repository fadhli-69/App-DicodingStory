package com.example.dicodingstory.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dicodingstory.R
import com.example.dicodingstory.data.remote.Story
import com.example.dicodingstory.databinding.ItemStoryBinding

class StoryAdapter(
    private val onItemClickListener: (Story, View, View, View) -> Unit
) : ListAdapter<Story, StoryAdapter.StoryViewHolder>(StoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoryViewHolder(binding, onItemClickListener)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StoryViewHolder(
        private val binding: ItemStoryBinding,
        private val onItemClickListener: (Story, View, View, View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.apply {
                ViewCompat.setTransitionName(itemPhoto, "image_${story.id}")
                ViewCompat.setTransitionName(itemName, "name_${story.id}")
                ViewCompat.setTransitionName(itemDescription, "description_${story.id}")

                itemName.text = story.name
                itemDescription.text = story.description

                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_error_image)
                    .into(itemPhoto)

                root.setOnClickListener {
                    onItemClickListener(story, itemPhoto, itemName, itemDescription)
                }
            }
        }
    }


    class StoryDiffCallback : DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }
}
