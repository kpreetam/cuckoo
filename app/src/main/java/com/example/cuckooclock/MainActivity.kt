package com.example.cuckooclock

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cuckooclock.databinding.ActivityMainBinding
import com.example.cuckooclock.fragments.ArtisanClockFragment
import com.example.cuckooclock.fragments.BitByteClockFragment
import com.example.cuckooclock.fragments.DigitalClockFragment
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var artisanFragment: ArtisanClockFragment? = null

    private val tickRunnable = object : Runnable {
        override fun run() {
            updateCurrentFragment()
            handler.postDelayed(this, 1000)
        }
    }

    private val chimeReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.example.cuckooclock.CHIME_EACH" -> {
    val index = intent.getIntExtra("cuckoo_index", 0)
    val total = intent.getIntExtra("cuckoo_total", 1)
    if (index == 0) {
        artisanFragment?.triggerAnimation(total)
    }
}
        }
    }
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        requestPermissions()
        setupTabs()
        ChimeScheduler.scheduleNextChime(this)
        binding.btnTest.setOnClickListener {
            val intent = Intent(this, ChimeService::class.java).apply {
                putExtra(ChimeScheduler.EXTRA_IS_HALF_HOUR, false)
                putExtra(ChimeScheduler.EXTRA_HOUR_COUNT, 3)
            }
            startForegroundService(intent)
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("🐦 Cuckoo"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Digital"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Binary"))

        val firstFragment = ArtisanClockFragment().also { artisanFragment = it }
        showFragment(firstFragment)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val fragment: Fragment = when (tab.position) {
                    0 -> ArtisanClockFragment().also { artisanFragment = it }
                    1 -> { artisanFragment = null; DigitalClockFragment() }
                    2 -> { artisanFragment = null; BitByteClockFragment() }
                    else -> ArtisanClockFragment().also { artisanFragment = it }
                }
                showFragment(fragment)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateCurrentFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        when (fragment) {
            is ArtisanClockFragment -> fragment.tick()
            is DigitalClockFragment -> fragment.tick()
            is BitByteClockFragment -> fragment.tick()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tickRunnable)
        val filter = IntentFilter("com.example.cuckooclock.CHIME_EACH")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(chimeReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(chimeReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tickRunnable)
        unregisterReceiver(chimeReceiver)
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissions.isNotEmpty())
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
