package id.antenaislam.airadio.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.antenaislam.airadio.model.Data
import id.antenaislam.airadio.repository.RadioRepository

class MainActivityViewModel : ViewModel() {
    private lateinit var radios: MutableLiveData<List<Data>>
    private var repo: RadioRepository = RadioRepository.getInstance()

    fun init() {
        radios = repo.getAllRadio()
    }

    fun getAllRadio(): LiveData<List<Data>> = radios
}