package com.example.cuckooclock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager


class ChimeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var toneGen: ToneGenerator? = null
    private var cuckooCount = 0
    private var totalCuckoos = 0
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    companion object {
        const val CHANNEL_ID = "cuckoo_chime_channel"
        const val NOTIF_ID = 1
        const val SOUND_CUCKOO = "cuckoo"
        const val SOUND_BELL = "bell"
        const val SOUND_CHIME = "chime"
        const val SOUND_WHISTLE = "whistle"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isHalf = intent?.getBooleanExtra(ChimeScheduler.EXTRA_IS_HALF_HOUR, false) ?: false
        val hourCount = intent?.getIntExtra(ChimeScheduler.EXTRA_HOUR_COUNT, 1) ?: 1

        startForeground(NOTIF_ID, buildNotification(isHalf, hourCount))

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val soundKey = if (isHalf) PrefsKeys.HALF_HOUR_CHIME_SOUND else PrefsKeys.HOUR_CHIME_SOUND
        val volumeKey = if (isHalf) PrefsKeys.HALF_HOUR_CHIME_VOLUME else PrefsKeys.HOUR_CHIME_VOLUME
        val sound = prefs.getString(soundKey, SOUND_CUCKOO) ?: SOUND_CUCKOO
        val volume = prefs.getInt(volumeKey, 80) / 100f

        totalCuckoos = if (isHalf) 1 else hourCount
        cuckooCount = 0

        requestAudioFocus(sound, volume)
        return START_NOT_STICKY
    }

    private fun getAudioStream(): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    return when (prefs.getString(PrefsKeys.CHIME_AUDIO_CHANNEL, "music")) {
        "alarm" -> AudioManager.STREAM_ALARM
        "notification" -> AudioManager.STREAM_NOTIFICATION
        else -> AudioManager.STREAM_MUSIC
    }
}

private fun requestAudioFocus(sound: String, volume: Float) {
    val stream = getAudioStream()
    val usage = when (stream) {
        AudioManager.STREAM_ALARM -> AudioAttributes.USAGE_ALARM
        AudioManager.STREAM_NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
        else -> AudioAttributes.USAGE_MEDIA
    }
    val focusType = when (stream) {
        AudioManager.STREAM_ALARM -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        else -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
    }

    val request = AudioFocusRequest.Builder(focusType)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(usage)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAcceptsDelayedFocusGain(false)
        .setOnAudioFocusChangeListener { }
        .build()

    focusRequest = request
    audioManager?.requestAudioFocus(request)
    playChimeSequence(sound, volume)
}

    private fun playChimeSequence(sound: String, volume: Float) {
        if (cuckooCount >= totalCuckoos) {
            abandonAudioFocus()
            stopSelf()
            return
        }
        playSingleChime(sound, volume) {
            cuckooCount++
            if (cuckooCount < totalCuckoos) {
                handler.postDelayed({ playChimeSequence(sound, volume) }, 600)
            } else {
                handler.postDelayed({
                    abandonAudioFocus()
                    stopSelf()
                }, 500)
            }
        }
    }

    private fun abandonAudioFocus() {
        focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private fun playSingleChime(sound: String, volume: Float, onComplete: () -> Unit) {
        when (sound) {
            SOUND_CUCKOO -> playSynthCuckoo(volume, onComplete)
            SOUND_BELL -> playSynthBell(volume, onComplete)
            SOUND_CHIME -> playSynthChime(volume, onComplete)
            SOUND_WHISTLE -> playSynthWhistle(volume, onComplete)
            else -> playSynthCuckoo(volume, onComplete)
        }
    }

private fun playSynthCuckoo(volume: Float, onComplete: () -> Unit) {
    try {
        val mp = MediaPlayer().apply {
            val afd = resources.openRawResourceFd(R.raw.cuckoo)
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setVolume(volume, volume)
            prepare()
        }
        mp.setOnCompletionListener {
            it.release()
            onComplete()
        }
        mp.start()
    } catch (e: Exception) {
        val volInt = (volume * 100).toInt().coerceIn(0, 100)
        toneGen = ToneGenerator(getAudioStream(), volInt)
        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        handler.postDelayed({
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
            handler.postDelayed({
                toneGen?.release()
                toneGen = null
                onComplete()
            }, 300)
        }, 250)
    }
}

    private fun playSynthBell(volume: Float, onComplete: () -> Unit) {
        val volInt = (volume * 100).toInt().coerceIn(0, 100)
        toneGen = ToneGenerator(getAudioStream(), volInt)
        toneGen?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 500)
        handler.postDelayed({
            toneGen?.release()
            toneGen = null
            onComplete()
        }, 550)
    }

    private fun playSynthChime(volume: Float, onComplete: () -> Unit) {
        val volInt = (volume * 100).toInt().coerceIn(0, 100)
        toneGen = ToneGenerator(getAudioStream(), volInt)
        toneGen?.startTone(ToneGenerator.TONE_SUP_PIP, 400)
        handler.postDelayed({
            toneGen?.release()
            toneGen = null
            onComplete()
        }, 450)
    }

    private fun playSynthWhistle(volume: Float, onComplete: () -> Unit) {
        val volInt = (volume * 100).toInt().coerceIn(0, 100)
        toneGen = ToneGenerator(getAudioStream(), volInt)
        toneGen?.startTone(ToneGenerator.TONE_DTMF_0, 350)
        handler.postDelayed({
            toneGen?.release()
            toneGen = null
            onComplete()
        }, 400)
    }

    private fun buildNotification(isHalf: Boolean, hourCount: Int): Notification {
        val text = if (isHalf) "Half-hour chime 🕐" else "Chiming $hourCount o'clock 🐦"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cuckoo Clock")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Cuckoo Chimes", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Hourly and half-hourly cuckoo chimes" }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        toneGen?.release()
        toneGen = null
        abandonAudioFocus()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
