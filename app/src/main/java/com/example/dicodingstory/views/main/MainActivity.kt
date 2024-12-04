package com.example.dicodingstory.views.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.dicodingstory.R
import com.example.dicodingstory.databinding.ActivityMainBinding
import com.example.dicodingstory.views.welcome.WelcomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setSupportActionBar(binding.toolbarMain)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.listStoryFragment -> showToolbar(true)
                else -> showToolbar(false)
            }
        }

        binding.toolbarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }

                else -> false
            }
        }

        observeLoginStatus()
        observeLogoutState()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private fun observeLoginStatus() {
        lifecycleScope.launch {
            mainViewModel.isUserLoggedIn().collect { isLoggedIn ->
                if (!isLoggedIn) {
                    navigateToWelcomeScreen()
                }
            }
        }
    }

    private fun observeLogoutState() {
        lifecycleScope.launch {
            mainViewModel.logoutState.collect { state ->
                when (state) {
                    is MainViewModel.LogoutState.Success -> {
                        Toast.makeText(this@MainActivity, "Logout berhasil", Toast.LENGTH_SHORT)
                            .show()
                        navigateToWelcomeScreen()
                    }

                    is MainViewModel.LogoutState.Error -> {
                        showErrorDialog(state.errorMessage)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun navigateToWelcomeScreen() {
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Logout Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                mainViewModel.logout()
            }
            .setNegativeButton("Tidak", null)
            .create()
            .show()
    }

    private fun showToolbar(show: Boolean) {
        if (show) {
            binding.toolbarMain.visibility = View.VISIBLE
        } else {
            binding.toolbarMain.visibility = View.GONE
        }
    }
}
