package id.antenaislam.airadio.fragment


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import id.antenaislam.airadio.BuildConfig
import id.antenaislam.airadio.R
import id.antenaislam.airadio.model.Radio
import id.antenaislam.airadio.service.NotificationService
import id.antenaislam.airadio.util.Player
import kotlinx.android.synthetic.main.fragment_player.*


class PlayerFragment : Fragment(), Player.PlayerListener {
    var radio: Radio? = null

    private val broadcast = Intent()
    private var player: Player? = null
    private lateinit var service: Intent
    private lateinit var receiver: BroadcastReceiver

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_TITLE = "extra_title"
        const val FILTER_ACTION = "action"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        service = Intent(context!!, NotificationService::class.java)
        receiver = PlayerReceiver()

        context?.registerReceiver(receiver, IntentFilter(FILTER_ACTION))

        tv_player_title.text = radio?.title

        Glide.with(context!!)
                .load(BuildConfig.BASE_URL + "assets/" + radio?.poster)
                .into(iv_player_poster)

        playRadio(radio)

        btn_play_pause.setOnClickListener {
            if (player!!.isPlaying) stopRadioAndNotification()
            else playRadio(radio)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRadioAndNotification()
        context?.unregisterReceiver(receiver)
    }

    override fun onConnectionFailed() {
        tv_player_description?.text = getString(R.string.failed_to_connect)
        broadcastMetadata(getString(R.string.failed_to_connect))
    }

    override fun onReceivedMetadata(title: String) {
        tv_player_description?.text = title
        broadcastMetadata(title)
    }

    override fun onNoTitleAvailable() {
        tv_player_description?.text = getString(R.string.no_title)
        context?.let {
            broadcastMetadata(it.resources.getString(R.string.no_title))
        }
    }

    private fun startNotificationService(radioName: String, title: String) {
        service.putExtra(EXTRA_NAME, radioName)
        service.putExtra(EXTRA_TITLE, title)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(context!!, service)
        else
            context?.startService(service)
    }

    private fun playRadio(radio: Radio?) {
        radio?.let {
            player?.let { player ->
                if (player.isSameRadio(it.url)) return
                if (player.isPlaying) player.stopPlayer()
            }
            player = Player(context!!, it.url, this)
            player?.startPlayer()
            swapButton()

            startNotificationService(it.title, getString(R.string.connecting))
        }
    }

    private fun stopRadio() {
        player?.let {
            if (it.isPlaying) {
                player?.stopPlayer()
                player?.clearNowPlaying()
                swapButton()
            }
        }
    }

    private fun stopRadioAndNotification() {
        stopRadio()
        broadcastStopNotification()
    }

    private fun swapButton() {
        btn_play_pause?.setImageDrawable(
                if (player!!.isPlaying) resources.getDrawable(R.drawable.ic_stop_white_24dp, null)
                else resources.getDrawable(R.drawable.ic_play_arrow_white_24dp, null)
        )
    }

    private fun broadcastMetadata(title: String) {
        broadcast.action = NotificationService.FILTER_METADATA
        broadcast.putExtra(NotificationService.FILTER_METADATA, title)
        context?.sendBroadcast(broadcast)
    }

    private fun broadcastStopNotification() {
        broadcast.action = NotificationService.FILTER_STOP
        context?.sendBroadcast(broadcast)
        context?.stopService(service)
    }

    inner class PlayerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == FILTER_ACTION) {
                    if (player!!.isPlaying) stopRadio()
                    else playRadio(radio)
                }
            }
        }
    }
}
