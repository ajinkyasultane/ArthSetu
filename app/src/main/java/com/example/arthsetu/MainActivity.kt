package com.example.arthsetu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.arthsetu.databinding.ActivityMainBinding
import com.example.arthsetu.work.WorkManagerScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var workManagerScheduler: WorkManagerScheduler

    // Permission launcher for SMS + Notifications
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
                && permissions[Manifest.permission.READ_SMS] == true
        if (smsGranted) {
            Toast.makeText(this, "SMS permission granted. Tracking transactions!", Toast.LENGTH_SHORT).show()
            workManagerScheduler.schedulePeriodicSync()
        } else {
            Toast.makeText(this, "SMS permission denied. Cannot track transactions.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        requestSmsPermissions()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun requestSmsPermissions() {
        val smsPermissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        // Notification permission required on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            smsPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = smsPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            // Already granted — start periodic sync
            workManagerScheduler.schedulePeriodicSync()
        } else {
            permissionLauncher.launch(smsPermissions.toTypedArray())
        }
    }
}