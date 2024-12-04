package com.example.dicodingstory.views.login

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
import com.example.dicodingstory.databinding.ActivityLoginBinding
import com.example.dicodingstory.views.main.MainActivity
import com.example.dicodingstory.views.signup.SignUpActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLoginButton()
        setupObservers()
        playAnimation()

        binding.registerTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)
            lifecycleScope.launch {
                loginViewModel.login(email, password)
            }
        }
    }

    private fun setupObservers() {
        loginViewModel.loginResult.observe(this) { result ->
            showLoading(false)

            result.onSuccess {
                showSuccessDialog()
            }.onFailure { exception ->
                showErrorDialog(exception.message ?: getString(R.string.unknown_error))
            }
        }

        loginViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.loginButton.isEnabled = !isLoading
        }
    }

    private fun showSuccessDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Login Success")
            .setMessage("Sekarang kamu bisa masuk")
            .setPositiveButton("OK") { _, _ ->
                navigateToMainActivity()
            }
            .create()

        dialog.setCancelable(false)
        dialog.show()
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

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val alphaAnimations = listOf(
            binding.titleTextView,
            binding.messageTextView,
            binding.emailTextView,
            binding.edLoginEmail,
            binding.passwordTextView,
            binding.edLoginPassword,
            binding.loginButton
        ).map { view ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).setDuration(200)
        }

        AnimatorSet().apply {
            playSequentially(alphaAnimations)
            startDelay = 100
        }.start()
    }
}
