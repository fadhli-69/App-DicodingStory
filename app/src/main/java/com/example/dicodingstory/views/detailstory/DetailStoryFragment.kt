package com.example.dicodingstory.views.detailstory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dicodingstory.databinding.FragmentDetailStoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailStoryFragment : Fragment() {

    private var _binding: FragmentDetailStoryBinding? = null
    private val binding get() = _binding!!

    private val args: DetailStoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        val story = args.story

        Glide.with(requireContext())
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)

        ViewCompat.setTransitionName(binding.ivDetailPhoto, "image_${story.id}")
        ViewCompat.setTransitionName(binding.tvDetailName, "name_${story.id}")
        ViewCompat.setTransitionName(binding.tvDetailDescription, "description_${story.id}")

        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description

        binding.ivDetailPhoto.post { startPostponedEnterTransition() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}