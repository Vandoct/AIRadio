package id.antenaislam.airadio.repository

import androidx.lifecycle.MutableLiveData
import id.antenaislam.airadio.api.RadioClient
import id.antenaislam.airadio.api.RadioService
import id.antenaislam.airadio.model.Data
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RadioRepository {
    private val service = RadioClient.getClient().create(RadioService::class.java)

    companion object {
        fun getInstance(): RadioRepository {
            return RadioRepository()
        }
    }

    fun getAllRadio(): MutableLiveData<List<Data>> {
        val data = MutableLiveData<List<Data>>()

        service.getAllRadio().enqueue(object : Callback<id.antenaislam.airadio.model.Response> {
            override fun onFailure(call: Call<id.antenaislam.airadio.model.Response>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<id.antenaislam.airadio.model.Response>, response: Response<id.antenaislam.airadio.model.Response>) {
                val result: id.antenaislam.airadio.model.Response? = response.body()
                result?.let {
                    data.value = it.data
                }
            }
        })

        return data
    }
}