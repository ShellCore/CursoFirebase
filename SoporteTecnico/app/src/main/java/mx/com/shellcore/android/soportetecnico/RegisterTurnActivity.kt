package mx.com.shellcore.android.soportetecnico

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_registerturn.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class RegisterTurnActivity : AppCompatActivity() {

    companion object {
        private const val F_MAIN_MESSAGE = "main_message"
        private const val F_SHOW_NAME = "show_name"
        private const val F_COLOR_PRIMARY = "color_primary"
        private const val F_COLOR_TEXT_MESSAGE = "color_text_message"
        private const val F_COLOR_BUTTON = "color_button"
        private const val F_COLOR_BUTTON_TEXT = "color_button_text"
    }

    private val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }


    private val mHideHandler = Handler()

    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private val mHideRunnable = Runnable { hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registerturn)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        configFirebaseRemoteConfig()
    }

    private fun configFirebaseRemoteConfig() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600L)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default)

        configFetch()
    }

    private fun configFetch() {
        firebaseRemoteConfig.fetch(0).addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseRemoteConfig.fetchAndActivate()
                Snackbar.make(
                        fullscreen_content_controls,
                        R.string.registerturn_message_remote_config,
                        Snackbar.LENGTH_LONG
                    )
                    .show()
            } else {
                Snackbar.make(
                        fullscreen_content_controls,
                        R.string.registerturn_message_local_config,
                        Snackbar.LENGTH_LONG
                    )
                    .show()
            }
        }

        displayMainMessage()
    }

    private fun displayMainMessage() {
        edtName.visibility =
            if (firebaseRemoteConfig.getBoolean(F_SHOW_NAME)) View.VISIBLE else View.GONE

        var messageRemote = firebaseRemoteConfig.getString(F_MAIN_MESSAGE)
        messageRemote = messageRemote.replace("\\n", "\n")
        fullscreen_content.text = messageRemote

        configColors()
    }

    private fun configColors() {
        contentMain.setBackgroundColor(
            Color.parseColor(
                firebaseRemoteConfig.getString(
                    F_COLOR_PRIMARY
                )
            )
        )

        fullscreen_content.setTextColor(
            Color.parseColor(
                firebaseRemoteConfig.getString(
                    F_COLOR_TEXT_MESSAGE
                )
            )
        )

        btnRequest.setBackgroundColor(Color.parseColor(firebaseRemoteConfig.getString(F_COLOR_BUTTON)))
        btnRequest.setTextColor(Color.parseColor(firebaseRemoteConfig.getString(F_COLOR_BUTTON_TEXT)))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, 100)
    }
}
