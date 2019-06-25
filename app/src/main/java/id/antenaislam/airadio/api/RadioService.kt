package id.antenaislam.airadio.api

import id.antenaislam.airadio.model.Response
import retrofit2.Call
import retrofit2.http.GET

interface RadioService {

    @GET("api/radio")
    fun getAllRadio(): Call<Response>

}