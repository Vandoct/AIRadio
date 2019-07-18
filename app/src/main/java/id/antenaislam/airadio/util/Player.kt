package id.antenaislam.airadio.util

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

class Player(context: Context, streamUrl: String, private val listener: PlayerListener) : Metadata.MetadataListener {
    private var mediaSource: ExtractorMediaSource? = null
    private val thread: Thread
    private val player = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(context),
            DefaultTrackSelector(), DefaultLoadControl())

    companion object {
        var NOW_PLAYING = ""
        var IS_PLAYING = false
    }

    init {
        player.playWhenReady = true

        val url = Uri.parse(streamUrl)
        NOW_PLAYING = streamUrl

        mediaSource = ExtractorMediaSource.Factory(
                DefaultHttpDataSourceFactory("exoplayer"))
                .createMediaSource(url)

        thread = Thread(Metadata(streamUrl, this))
        thread.start()
    }

    override fun onConnectionFailed() {
        listener.onConnectionFailed()
    }

    override fun onNewTitleReceived(title: String) {
        listener.onReceivedMetadata(title)
    }

    override fun onNoTitleAvailable() {
        listener.onNoTitleAvailable()
    }

    fun startPlayer() {
        player.prepare(mediaSource, true, false)
        IS_PLAYING = true
    }

    fun stopPlayer() {
        thread.interrupt()
        player.stop()
        player.release()
        IS_PLAYING = false
    }

    fun isSameRadio(url: String) = url == NOW_PLAYING

    fun clearNowPlaying() {
        NOW_PLAYING = ""
    }

    interface PlayerListener {
        fun onConnectionFailed()
        fun onReceivedMetadata(title: String)
        fun onNoTitleAvailable()
    }
}