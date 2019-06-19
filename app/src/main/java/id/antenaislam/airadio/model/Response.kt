package id.antenaislam.airadio.model

import com.google.gson.annotations.SerializedName

data class Response(val status: String, val data: List<Data>)

data class Data(val category: String, val radio: List<Radio>)

data class Radio(val id: Int, val title: String, val url: String, @SerializedName("path") val poster: String)