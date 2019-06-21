package id.antenaislam.airadio.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import id.antenaislam.airadio.BuildConfig
import id.antenaislam.airadio.R
import id.antenaislam.airadio.util.Player
import kotlinx.android.synthetic.main.fragment_player.*


class PlayerFragment : Fragment(), Player.MetadataListener {
    private var player: Player? = null

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_POSTER = "extra_poster"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(EXTRA_TITLE)
        val url = arguments?.getString(EXTRA_URL)!!
        val poster = arguments?.getString(EXTRA_POSTER)

        tv_player_title.text = title

        Glide.with(context!!)
                .load(BuildConfig.BASE_URL + "assets/" + poster)
                .into(iv_player_poster)

        playRadio(url)

        /**
         *
         * TODO: Notification
         * TODO: Theme
         *
         */

        btn_play_pause.setOnClickListener {
            if (player!!.isPlaying) stopRadio()
            else playRadio(url)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRadio()
    }

    override fun onReceivedMetadata(title: String) {
        tv_player_description.text = title
    }

    private fun playRadio(url: String) {
        player?.let {
            if (it.isSameRadio(url)) return
            if (it.isPlaying) it.stopPlayer()
        }
        player = Player(context!!, url, this)
        player?.startPlayer()
        swapButton()
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

    private fun swapButton() {
        btn_play_pause?.setImageDrawable(
                if (player!!.isPlaying) resources.getDrawable(R.drawable.ic_stop_white_24dp, null)
                else resources.getDrawable(R.drawable.ic_play_arrow_white_24dp, null)
        )
    }
}
