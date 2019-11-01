package com.shellcore.tech.ofertas

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendintIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationManager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_shop)
            .setContentTitle("Hola")
            .setContentText("Bienvenido")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendintIntent)

        val channelId = getString(R.string.normal_channel_id)
        val channelName = getString(R.string.normal_channel_name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 200, 50)

            notificationManager?.createNotificationChannel(channel)

            notificationBuilder.setChannelId(channelId)
        }

        notificationManager?.notify("", 0, notificationBuilder.build())
    }
}
