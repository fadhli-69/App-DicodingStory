package com.example.dicodingstory.views.liststory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dicodingstory.R
import com.example.dicodingstory.databinding.FragmentListStoryBinding
import com.example.dicodingstory.views.adapter.StoryAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListStoryFragment : Fragment() {

    private var _binding: FragmentListStoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ListStoryViewModel by viewModels()
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToRefresh()
        observeViewModel()
        viewModel.fetchStories()
        setupFabClickListener()
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { story, itemPhoto, itemName, itemDescription ->
            val extras = FragmentNavigatorExtras(
                itemPhoto to "image_${story.id}",
                itemName to "name_${story.id}",
                itemDescription to "description_${story.id}"
            )

            val action =
                ListStoryFragmentDirections.actionListStoryFragmentToDetailStoryFragment(story)
            findNavController().navigate(action, extras)
        }

        binding.rvStoryList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = storyAdapter
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchStories()
        }
    }

    private fun setupFabClickListener() {
        binding.fabAddStory.setOnClickListener {
            findNavController().navigate(R.id.action_listStoryFragment_to_addStoryFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.stories.observe(viewLifecycleOwner) { stories ->
            storyAdapter.submitList(stories)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
