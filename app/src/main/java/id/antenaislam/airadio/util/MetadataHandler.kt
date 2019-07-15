package id.antenaislam.airadio.util

import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference

class MetadataHandler(listener: Metadata.MetadataListener) : Handler() {
    private val ref = WeakReference(listener)

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)

        val listener = ref.get()

        when (msg.what) {
            Metadata.ACTION_CONNECTION_FAILURE -> listener?.onConnectionFailed()
            Metadata.ACTION_NEW_TITLE -> listener?.onNewTitleReceived(msg.obj.toString())
            Metadata.ACTION_NO_TITLE -> listener?.onNoTitleAvailable()
        }
    }
}