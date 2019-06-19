package id.antenaislam.airadio.util

import android.util.Log
import org.jetbrains.anko.doAsync
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class Metadata {
    private var metadata: Map<String, String>? = null
    private var data: Map<String, String>? = null
    var streamUrl: URL? = null

    /**
     * Get artist using stream's title
     *
     * @return String
     * @throws IOException
     */
    val artist: String
        @Throws(IOException::class)
        get() {
            data = getMetadata()

            if (!data!!.containsKey("StreamTitle")) return ""

            val streamTitle = data!!["StreamTitle"]
            val title = streamTitle!!.substring(0, streamTitle.indexOf("-"))
            return title.trim { it <= ' ' }
        }

    /**
     * Get streamTitle
     *
     * @return String
     * @throws IOException
     */
    val streamTitle: String
        @Throws(IOException::class)
        get() {
            data = getMetadata()

            return if (!data!!.containsKey("StreamTitle")) "" else data!!["StreamTitle"].toString()

        }

    /**
     * Get title using stream's title
     *
     * @return String
     * @throws IOException
     */
    val title: String
        @Throws(IOException::class)
        get() {
            data = getMetadata()

            if (!data!!.containsKey("StreamTitle")) return ""

            val streamTitle = data!!["StreamTitle"]
            val artist = streamTitle!!.substring(streamTitle.indexOf("-") + 1)
            return artist.trim { it <= ' ' }
        }

    private fun parseMetadata(metaString: String): Map<String, String> {
        val metadata = HashMap<String, String>()
        val metaParts = metaString.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val p = Pattern.compile("^([a-zA-Z]+)='([^']*)'$")
        var m: Matcher
        for (i in metaParts.indices) {
            m = p.matcher(metaParts[i])
            if (m.find()) {
                metadata[m.group(1) as String] = m.group(2) as String
            }
        }

        return metadata
    }

    @Throws(IOException::class)
    fun getMetadata(): Map<String, String>? {
        if (metadata == null) {
            refreshMeta()
        }

        return metadata
    }

    @Synchronized
    @Throws(IOException::class)
    fun refreshMeta() {
        doAsync {
            retreiveMetadata()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    private fun retreiveMetadata() {
        val con = this.streamUrl!!.openConnection()
        con.setRequestProperty("Icy-MetaData", "1")
        con.setRequestProperty("Connection", "close")
        con.setRequestProperty("Accept", null)
        con.connect()

        var metaDataOffset = 0
        val headers = con.headerFields
        val stream = con.getInputStream()

        if (headers.containsKey("icy-metaint")) {
            // Headers are sent via HTTP
            metaDataOffset = Integer.parseInt(headers["icy-metaint"]!![0])
        } else {
            // Headers are sent within a stream
            val strHeaders = StringBuilder()

            while (true) {
                val c = stream.read().toChar().toInt()

                if (c != -1) {
                    strHeaders.append(c)
                    if (strHeaders.length > 5 && strHeaders.substring(strHeaders.length - 4, strHeaders.length) == "\r\n\r\n") {
                        break
                    }
                } else break
            }

            // Match headers to get metadata offset within a stream
            val p = Pattern.compile("\\r\\n(icy-metaint):\\s*(.*)\\r\\n")
            val m = p.matcher(strHeaders.toString())
            if (m.find()) {
                metaDataOffset = Integer.parseInt(m.group(2)!!)
            }
        }

        // In case no data was sent
        if (metaDataOffset == 0) return

        // Read metadata
        var count = 0
        var metaDataLength = 4080 // 4080 is the max length
        val metaData = StringBuilder()
        // Stream position should be either at the beginning or right after headers

        while (true) {
            val b = stream.read()

            if (b != -1) {
                count++

                // Length of the metadata
                if (count == metaDataOffset + 1) {
                    metaDataLength = b * 16
                }

                val inData = count > metaDataOffset + 1 && count < metaDataOffset + metaDataLength

                if (inData && b != 0) {
                    metaData.append(b.toChar())
                }

                if (count > metaDataOffset + metaDataLength) {
                    break
                }
            } else break
        }

        // Set the data
        metadata = parseMetadata(metaData.toString())

        // Close
        stream.close()
    }
}
