package com.example.dicodingstory.views.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.dicodingstory.R
import com.example.dicodingstory.databinding.ActivitySignUpBinding
import com.example.dicodingstory.views.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSignUpButton()
        setupObservers()
        playAnimation()
    }

    private fun setupSignUpButton() {
        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            val isNameValid = binding.edRegisterName.text?.isNotEmpty() == true
            val isEmailValid = binding.edRegisterEmail.error == null
            val isPasswordValid = binding.edRegisterPassword.error == null

            if (isNameValid && isEmailValid && isPasswordValid) {
                // Tampilkan loading
                showLoading(true)

                lifecycleScope.launch {
                    signUpViewModel.register(name, email, password)
                }
            } else {
                Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        signUpViewModel.registerResult.observe(this) { result ->
            showLoading(false)

            result.onSuccess {
                showSuccessDialog()
            }.onFailure { exception ->
                showErrorDialog(exception.message ?: "Registration Failed")
            }
        }

        signUpViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.signupButton.isEnabled = !isLoading
        }
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.signupButton.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Registration Success")
            .setMessage("You can now log in")
            .setPositiveButton("OK") { _, _ ->
                navigateToLoginActivity()
            }
            .create()
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val alphaAnimations = listOf(
            binding.titleTextView,
            binding.nameTextView,
            binding.edRegisterName,
            binding.emailTextView,
            binding.edRegisterEmail,
            binding.passwordTextView,
            binding.edRegisterPassword,
            binding.signupButton
        ).map { view ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).setDuration(200)
        }

        AnimatorSet().apply {
            playSequentially(alphaAnimations)
            startDelay = 100
        }.start()
    }
}
