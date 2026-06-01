package com.example.cuckooclock

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import com.example.cuckooclock.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
            .replace(R.id.settingsContainer, SettingsFragment())
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupDndOverride()
        bindSummary(PrefsKeys.BEDTIME_START)
        bindSummary(PrefsKeys.BEDTIME_END)
    }

    private fun bindSummary(key: String) {
        val pref = findPreference<EditTextPreference>(key) ?: return
        pref.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
    }

    private fun setupDndOverride() {
        val dndPref = findPreference<SwitchPreferenceCompat>(PrefsKeys.OVERRIDE_SILENT)
        dndPref?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                val nm = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (!nm.isNotificationPolicyAccessGranted) {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    false
                } else true
            } else true
        }
    }
}
}
