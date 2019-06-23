package id.antenaislam.airadio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import id.antenaislam.airadio.R
import id.antenaislam.airadio.fragment.PlayerFragment

class NotificationService : Service() {
    private lateinit var receiver: BroadcastReceiver
    private lateinit var builder: NotificationCompat.Builder

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "channel_01"
        const val CHANNEL_NAME = "Player"
        const val FILTER_METADATA = "metadata"
        const val FILTER_STOP = "stop"
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val radioName = intent?.getStringExtra(PlayerFragment.EXTRA_NAME)
        val title = intent?.getStringExtra(PlayerFragment.EXTRA_TITLE)

        if (radioName != null && title != null) startNotification(radioName, title)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, itn: Intent?) {
                itn?.let {
                    if (it.action == FILTER_STOP) unregisterReceiver(receiver)
                    else itn.getStringExtra(FILTER_METADATA)?.let { title -> updateNotification(title) }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(FILTER_METADATA)
        filter.addAction(FILTER_STOP)

        registerReceiver(receiver, filter)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private fun startNotification(radioName: String, title: String) {
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setContentTitle(radioName)
                .setContentText(title)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startCustomNotification()

        startForeground(NOTIFICATION_ID, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCustomNotification() {
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        service.createNotificationChannel(channel)
    }

    private fun updateNotification(title: String) {
        builder.setContentText(title)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startCustomNotification()

        startForeground(NOTIFICATION_ID, builder.build())
    }
}