package id.antenaislam.airadio.preference

import android.content.Context
import android.content.SharedPreferences

class SettingPreference(context: Context) {
    private val preferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "settings_pref"
        const val FIRST_TIME = "first_time"
        const val THEME = "theme"
    }

    fun setFirstTime(isFirstTime: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(FIRST_TIME, isFirstTime)
        editor.apply()
    }

    fun isFirstTime(): Boolean = preferences.getBoolean(FIRST_TIME, true)

    fun setTheme(theme: String) {
        val editor = preferences.edit()
        editor.putString(THEME, theme)
        editor.apply()
    }

    fun getTheme(): String? = preferences.getString(THEME, null)
}