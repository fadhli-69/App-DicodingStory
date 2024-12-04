package com.example.dicodingstory.views.addstory

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dicodingstory.databinding.FragmentAddStoryBinding
import com.example.dicodingstory.reduceFileImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class AddStoryFragment : Fragment() {
    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!

    private val addStoryViewModel: AddStoryViewModel by viewModels()

    private var currentPhotoPath: String? = null
    private var imageFile: File? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                val file = File(path)
                imageFile = file.reduceFileImage()
                Glide.with(requireContext())
                    .load(imageFile)
                    .into(binding.previewImageView)
            }
        }
    }

    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                try {
                    imageFile = null
                    val inputStream = requireContext().contentResolver.openInputStream(selectedUri)
                    val tempFile = File(
                        requireContext().cacheDir,
                        "selected_image_${System.currentTimeMillis()}.jpg"
                    )
                    tempFile.outputStream().use { fileOut ->
                        inputStream?.copyTo(fileOut)
                    }

                    imageFile = tempFile.reduceFileImage()

                    Glide.with(requireContext())
                        .load(imageFile)
                        .into(binding.previewImageView)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Error selecting image: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observeViewModel()
    }

    private fun setupButtons() {
        binding.cameraButton.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.galleryButton.setOnClickListener {
            photoPicker.launch("image/*")
        }

        binding.buttonAdd.setOnClickListener {
            uploadStory()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addStoryViewModel.uploadState.collect { state ->
                    when (state) {
                        is AddStoryUiState.Initial -> {
                            binding.progressBar.visibility = View.GONE
                            binding.buttonAdd.isEnabled = true
                        }

                        is AddStoryUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.buttonAdd.isEnabled = false
                        }

                        is AddStoryUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.buttonAdd.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT)
                                .show()
                            findNavController().navigateUp()
                        }

                        is AddStoryUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.buttonAdd.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString().trim()

        if (description.isBlank()) {
            Toast.makeText(requireContext(), "Description cannot be empty", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (imageFile == null) {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())
        val imageRequestBody = imageFile!!.asRequestBody("image/jpeg".toMediaType())
        val imageMultipart = MultipartBody.Part.createFormData(
            "photo",
            imageFile!!.name,
            imageRequestBody
        )

        addStoryViewModel.uploadStory(descriptionRequestBody, imageMultipart)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        currentPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(photoUri)
    }

    private fun createImageFile(): File {
        val timeStamp = System.currentTimeMillis().toString()
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}