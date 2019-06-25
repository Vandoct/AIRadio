package id.antenaislam.airadio.util

import android.os.Bundle
import android.os.Message
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class Metadata(private val url: String, listener: MetadataListener) : Runnable {
    private val handler = MetadataHandler(listener)

    companion object {
        const val TAG = "Metadata"
        const val WINDOWS_MEDIA_PLAYER = "Windows-Media-Player/11.0.5721.5145"
        const val VLC = "vlc 1.1.0-git-20100330-0003"
        const val AIMP = "BASS/2.4"
        const val MOZILLA = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0"
        const val CHROME = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"
        const val OPERA = "Opera/9.80 (Windows NT 6.2; WOW64) Presto/2.12.388 Version/12.17"
        const val SAFARI = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2"
        const val IE = "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; ASU2JS; rv:11.0) like Gecko"
        const val BR = "icy-br"
        const val NAME = "icy-name"
        const val GENRE = "icy-genre"
        const val INFO = "ice-audio-info"
        const val DESC = "icy-description"
        const val ACTION_CONNECTION_FAILURE = 0
        const val ACTION_NEW_TITLE = 1
        const val ACTION_NO_TITLE = 2
    }

    override fun run() {
        retrieveMetadata()
    }

    private fun retrieveMetadata() {
        val urlConnection: HttpURLConnection

        try {
            urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("User-Agent", VLC)
            urlConnection.setRequestProperty("Icy-MetaData", "1")
            urlConnection.connect()
        } catch (e: IOException) {
            val msg = Message.obtain()
            msg.what = ACTION_CONNECTION_FAILURE
            handler.sendMessage(msg)

            Log.e(TAG, "Failed to open connection")
            e.printStackTrace()
            return
        }

        val headers = urlConnection.headerFields
        val headersData = Bundle()

        headersData.putStringArrayList(NAME,
                if (headers[NAME] != null) ArrayList(headers[NAME]!!) else ArrayList())
        headersData.putStringArrayList(DESC,
                if (headers[DESC] != null) ArrayList(headers[DESC]!!) else ArrayList())
        headersData.putStringArrayList(BR,
                if (headers[BR] != null) ArrayList(headers[BR]!!) else ArrayList())
        headersData.putStringArrayList(GENRE,
                if (headers[GENRE] != null) ArrayList(headers[GENRE]!!) else ArrayList())
        headersData.putStringArrayList(INFO,
                if (headers[INFO] != null) ArrayList(headers[INFO]!!) else ArrayList())

        if (!headers.containsKey("icy-metaint")) {
            val msg = Message.obtain()
            msg.what = ACTION_NO_TITLE
            handler.sendMessage(msg)

            Log.i(TAG, "IceCast server doesn't support metadata")
            urlConnection.disconnect()
            return
        }

        val icyMetaInt = Integer.parseInt(urlConnection.getHeaderField("icy-metaint"))
        val stream: InputStream

        try {
            stream = urlConnection.inputStream
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "InputStream can not be created")
            urlConnection.disconnect()
            return
        }

        val baos = ByteArrayOutputStream()

        try {
            while (!Thread.interrupted()) {
                var skipped = stream.skip(icyMetaInt.toLong())

                while (skipped != icyMetaInt.toLong()) {
                    skipped += stream.skip(icyMetaInt - skipped)
                }

                val symbolLength = stream.read()
                val metaDataLength = symbolLength * 16

                if (metaDataLength > 0) {
                    for (i in 0 until metaDataLength) {
                        val metaDataSymbol = stream.read()

                        if (metaDataSymbol > 0) baos.write(metaDataSymbol)
                    }

                    val result = parseMetadata(baos.toString())
                    baos.reset()

                    if (result == "") {
                        val msg = Message.obtain()
                        msg.what = ACTION_NO_TITLE
                        handler.sendMessage(msg)

                        return
                    }

                    val msg = Message.obtain()
                    msg.what = ACTION_NEW_TITLE
                    msg.obj = result
                    handler.sendMessage(msg)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Thread Interrupted!")
        } finally {
            try {
                baos.close()
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        urlConnection.disconnect()
    }

    private fun parseMetadata(result: String): String {
        var temp = result
                .replace("StreamTitle=", "")
                .replace("StreamUrl=", "")
                .replace("'".toRegex(), "")
                .replace(";".toRegex(), "")

        if (temp.isNotEmpty() && temp.substring(temp.length - 1) == "-") {
            temp = temp.substring(0, temp.length - 1).trim()
        }

        return temp
    }

    interface MetadataListener {
        fun onConnectionFailed()
        fun onNewTitleReceived(title: String)
        fun onNoTitleAvailable()
    }
}
