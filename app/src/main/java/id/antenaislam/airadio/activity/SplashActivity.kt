package id.antenaislam.airadio.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.antenaislam.airadio.preference.SettingPreference

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = SettingPreference(this)

        if (pref.isFirstTime()) {
            pref.setFirstTime(false)
            pref.setTheme(MainActivity.THEME_DARK)
        }

        super.onCreate(savedInstanceState)

        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}