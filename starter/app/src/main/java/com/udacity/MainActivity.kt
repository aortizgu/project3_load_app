package com.udacity

import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var downloadManager: DownloadManager
    private var url = ""
    private var currentDownload: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            when {
                url.isEmpty() -> {
                    Toast.makeText(
                        this,
                        getString(R.string.no_url_selected),
                        Toast.LENGTH_LONG
                    ).show()
                }
                currentDownload > 0 -> {
                    Toast.makeText(
                        this,
                        getString(R.string.already_downloading),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    download()
                }
            }
        }
        notificationManager = getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        createChannel(CHANNEL_ID, CHANNEL_NAME)
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            if (view.isChecked) {
                url = view.text as String
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                Log.d("BroadcastReceiver", "receive $id")
                downloadManager.query(DownloadManager.Query().setFilterById(id)).use {
                    if (it.moveToFirst()) {
                        custom_button.buttonState = ButtonState.Completed
                        custom_button.progress = 0f
                        currentDownload = 0
                        notificationManager.sendNotification(
                            getText(R.string.notification_description).toString(),
                            applicationContext,
                            getString(R.string.app_name),
                            when (it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                                DownloadManager.STATUS_SUCCESSFUL -> "Success"
                                DownloadManager.STATUS_FAILED -> "Failed"
                                else -> "Unknown"
                            }
                        )
                    }
                }
            }
        }
    }

    private fun startDownload() {
        custom_button.buttonState = ButtonState.Loading
        val animator = ObjectAnimator.ofFloat(custom_button, "progress", 1f)
        animator.duration = LOADING_MILLISECONDS
        animator.start()
    }

    private fun download() {
        custom_button.buttonState = ButtonState.Clicked
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        currentDownload = downloadManager.enqueue(request)
        startDownload()
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun NotificationManager.sendNotification(
        messageBody: String,
        applicationContext: Context,
        filename: String,
        status: String
    ) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra(FILENAME_KEY, filename)
        contentIntent.putExtra(STATUS_KEY, status)

        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.check),
                pendingIntent)

        notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val URL =
            "https://github.com/FFmpeg/FFmpeg/releases/download/n3.0/ffmpeg-3.0.tar.bz2"
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "channelName"
        private const val NOTIFICATION_ID = 0
        private const val LOADING_MILLISECONDS = 1000L
    }

}
