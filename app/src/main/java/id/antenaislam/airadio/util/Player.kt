package id.antenaislam.airadio.util

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.vodyasov.amr.AudiostreamMetadataManager
import com.vodyasov.amr.OnNewMetadataListener
import com.vodyasov.amr.UserAgent

class Player(context: Context, streamUrl: String, private val listener: MetadataListener) : OnNewMetadataListener {

    var isPlaying = true

    private var mediaSource: ExtractorMediaSource? = null
    private val metadataManager = AudiostreamMetadataManager.getInstance()
    private val player = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(context),
            DefaultTrackSelector(), DefaultLoadControl())

    companion object {
        var NOW_PLAYING = ""
    }

    init {
        player.playWhenReady = true

        val url = Uri.parse(streamUrl)
        NOW_PLAYING = streamUrl

        mediaSource = ExtractorMediaSource.Factory(
                DefaultHttpDataSourceFactory("exoplayer"))
                .createMediaSource(url)

        metadataManager.setUri(url)
                .setOnNewMetadataListener(this)
                .setUserAgent(UserAgent.VLC)
                .start()
    }

    override fun onNewHeaders(stringUri: String?, name: MutableList<String>?, desc: MutableList<String>?, br: MutableList<String>?, genre: MutableList<String>?, info: MutableList<String>?) {
    }

    override fun onNewStreamTitle(stringUri: String?, streamTitle: String?) {
        streamTitle?.let {
            listener.onReceivedMetadata(it)
        }
    }

    fun startPlayer() {
        player.prepare(mediaSource, true, false)
    }

    fun stopPlayer() {
        player.stop()
        player.release()
        metadataManager.stop()
        isPlaying = false
    }

    fun isSameRadio(url: String) = url == NOW_PLAYING

    fun clearNowPlaying() {
        NOW_PLAYING = ""
    }

    interface MetadataListener {
        fun onReceivedMetadata(title: String)
    }
}