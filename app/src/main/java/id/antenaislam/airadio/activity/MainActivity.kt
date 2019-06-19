package id.antenaislam.airadio.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import id.antenaislam.airadio.R
import id.antenaislam.airadio.adapter.MainAdapter
import id.antenaislam.airadio.adapter.RadioAdapter
import id.antenaislam.airadio.fragment.PlayerFragment
import id.antenaislam.airadio.model.Radio
import id.antenaislam.airadio.util.Player
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), RadioAdapter.PlayerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.init()
        viewModel.getAllRadio().observe(this, Observer {
            rv_main.adapter = MainAdapter(it, this)
        })

        rv_main.layoutManager = LinearLayoutManager(this)
    }

    override fun onRadioClicked(radio: Radio) {
        if (radio.url == Player.NOW_PLAYING) return

        val bundle = Bundle()
        bundle.putString(PlayerFragment.EXTRA_TITLE, radio.title)
        bundle.putString(PlayerFragment.EXTRA_URL, radio.url)
        bundle.putString(PlayerFragment.EXTRA_POSTER, radio.poster)

        val fragment = PlayerFragment()
        fragment.arguments = bundle

        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.container_player, fragment)
        fm.commit()
    }
}
