package id.antenaislam.airadio.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import id.antenaislam.airadio.R
import id.antenaislam.airadio.fragment.PlayerFragment

class NotificationService : Service() {
    private var isPlaying = true
    private lateinit var receiver: BroadcastReceiver
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var pending: PendingIntent
    private lateinit var notificationLayout: RemoteViews
    private lateinit var notificationLayoutExpanded: RemoteViews

    companion object {
        const val REQUEST_CODE = 101
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "channel_01"
        const val CHANNEL_NAME = "Player"
        const val FILTER_METADATA = "metadata"
        const val FILTER_STOP = "stop"
        const val FILTER_NOTIFICATION_ACTION = "notification_action"
    }

    override fun onCreate() {
        super.onCreate()

        receiver = NotificationListener()

        val filter = IntentFilter()
        filter.addAction(FILTER_METADATA)
        filter.addAction(FILTER_STOP)
        filter.addAction(FILTER_NOTIFICATION_ACTION)

        pending = PendingIntent.getBroadcast(this, REQUEST_CODE, Intent(FILTER_NOTIFICATION_ACTION), 0)

        registerReceiver(receiver, filter)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val radioName = intent?.getStringExtra(PlayerFragment.EXTRA_NAME)
        val title = intent?.getStringExtra(PlayerFragment.EXTRA_TITLE)

        if (radioName != null && title != null) startNotification(radioName, title)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private fun startNotification(radioName: String, title: String) {
        notificationLayout = RemoteViews(packageName, R.layout.custom_notification)
        notificationLayout.setTextViewText(R.id.tv_notification_name, radioName)
        notificationLayout.setTextViewText(R.id.tv_notification_title, title)
        notificationLayout.setOnClickPendingIntent(R.id.btn_notification_play_pause, pending)

        notificationLayoutExpanded = RemoteViews(packageName, R.layout.custom_notification_expanded)
        notificationLayoutExpanded.setTextViewText(R.id.tv_notification_name, radioName)
        notificationLayoutExpanded.setTextViewText(R.id.tv_notification_title, title)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.btn_notification_play_pause, pending)


        builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)

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
        notificationLayout.setTextViewText(R.id.tv_notification_title, title)
        notificationLayoutExpanded.setTextViewText(R.id.tv_notification_title, title)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startCustomNotification()

        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun onButtonPressed() {
        swapButton()
        sendBroadcast(Intent(PlayerFragment.FILTER_ACTION))
    }

    private fun swapButton() {
        if (isPlaying) {
            notificationLayout.setImageViewResource(R.id.btn_notification_play_pause, R.drawable.ic_play_arrow_white_24dp)
            notificationLayoutExpanded.setImageViewResource(R.id.btn_notification_play_pause, R.drawable.ic_play_arrow_white_24dp)
        } else {
            notificationLayout.setImageViewResource(R.id.btn_notification_play_pause, R.drawable.ic_stop_white_24dp)
            notificationLayoutExpanded.setImageViewResource(R.id.btn_notification_play_pause, R.drawable.ic_stop_white_24dp)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startCustomNotification()

        startForeground(NOTIFICATION_ID, builder.build())

        isPlaying = !isPlaying
    }

    inner class NotificationListener : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    FILTER_STOP -> unregisterReceiver(receiver)
                    FILTER_METADATA -> intent.getStringExtra(FILTER_METADATA)?.let { title -> updateNotification(title) }
                    FILTER_NOTIFICATION_ACTION -> onButtonPressed()
                    else -> {
                    }
                }
            }
        }
    }
}