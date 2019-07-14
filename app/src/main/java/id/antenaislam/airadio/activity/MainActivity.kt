package id.antenaislam.airadio.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import id.antenaislam.airadio.R
import id.antenaislam.airadio.adapter.MainAdapter
import id.antenaislam.airadio.adapter.RadioAdapter
import id.antenaislam.airadio.fragment.PlayerFragment
import id.antenaislam.airadio.model.Radio
import id.antenaislam.airadio.preference.SettingPreference
import id.antenaislam.airadio.util.Player
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), RadioAdapter.PlayerListener {
    private var isLightTheme = false
    private lateinit var preference: SettingPreference

    companion object {
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        preference = SettingPreference(this)
        isLightTheme = preference.getTheme() == THEME_LIGHT
        setTheme(
                if (isLightTheme) R.style.AppTheme
                else R.style.DarkTheme
        )

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

        val fragment = PlayerFragment()
        fragment.radio = radio

        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.container_player, fragment)
        fm.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.theme_menu, menu)

        menu?.findItem(R.id.theme_switcher)?.setIcon(
                if (isLightTheme) R.drawable.ic_light_theme_24dp
                else R.drawable.ic_dark_theme_24dp
        )

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.theme_switcher -> if (isLightTheme) {
                item.setIcon(R.drawable.ic_dark_theme_24dp)
                preference.setTheme(THEME_DARK)
                this.recreate()
            } else {
                item.setIcon(R.drawable.ic_light_theme_24dp)
                preference.setTheme(THEME_LIGHT)
                this.recreate()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
