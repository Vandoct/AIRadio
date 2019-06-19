package id.antenaislam.airadio.api

import id.antenaislam.airadio.model.Response
import retrofit2.Call
import retrofit2.http.GET

interface RadioService {

    @GET("api/table")
    fun getAllTableName(): Call<List<String>>

    @GET("api/radio")
    fun getAllRadio(): Call<Response>

    @GET("api/radio/anime")
    fun getAnimeRadio(): Call<Response>

}