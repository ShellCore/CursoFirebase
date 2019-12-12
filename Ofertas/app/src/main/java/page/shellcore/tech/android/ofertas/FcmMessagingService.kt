package page.shellcore.tech.android.ofertas

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val DESCUENTO = "descuento"

class FcmMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty() && remoteMessage.notification != null) {
            sendNotification(remoteMessage)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val desc = remoteMessage.data[DESCUENTO]!!.toFloat()

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(DESCUENTO, desc)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_shop)
            .setContentTitle(remoteMessage.notification!!.title)
            .setContentText(remoteMessage.notification!!.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color = ContextCompat.getColor(applicationContext, (if (desc > 0.4) R.color.colorPrimary else R.color.colorAccent))
            notificationBuilder.setColor(color)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = if (desc < 0.1) getString(R.string.low_channel_id) else getString(R.string.normal_channel_id)
            val channelName = if (desc < 0.1) getString(R.string.low_channel_name) else getString(R.string.normal_channel_name)
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100L, 200L, 200L, 50L)
            notificationManager.createNotificationChannel(channel)
            notificationBuilder.setChannelId(channelId)
        }

        notificationManager.notify("", 0, notificationBuilder.build())
    }
}
