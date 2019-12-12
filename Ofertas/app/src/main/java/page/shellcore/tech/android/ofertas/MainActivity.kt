package page.shellcore.tech.android.ofertas

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*

private const val SP_TOPICS = "sharedPreferencesTopics"


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mTopicsSet: Set<String>

    private val mSharedPreferences: SharedPreferences by lazy {
        getPreferences(Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSubscribe.setOnClickListener(this)
        btnUnsubscribe.setOnClickListener(this)

        configureSharedPreferences()

        if (FirebaseInstanceId.getInstance().token != null) {
            Log.i("Token MainActivity", FirebaseInstanceId.getInstance().token)
        }
    }

    private fun configureSharedPreferences() {
        mTopicsSet = mSharedPreferences.getStringSet(SP_TOPICS, HashSet<String>())!!
        showTopics()
    }

    private fun showTopics() {
        txtTopics.text = mTopicsSet.toString()
    }

    override fun onClick(view: View) {
        val topic = resources.getStringArray(R.array.topicsValues)[spnTopics.selectedItemPosition]

        when (view.id) {
            R.id.btnSubscribe -> suscribe(topic)
            R.id.btnUnsubscribe -> unsuscribe(topic)
        }

    }

    private fun suscribe(topic: String) {
        if (!mTopicsSet.contains(topic)) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        (mTopicsSet as HashSet).add(topic)
                        saveSharedPreferences()
                        Toast.makeText(this, "Suscrito a $topic", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Error de suscripción", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    private fun saveSharedPreferences() {
        mSharedPreferences.edit().apply {
            clear()
            putStringSet(SP_TOPICS, mTopicsSet)
            apply()
        }

        showTopics()
    }

    private fun unsuscribe(topic: String?) {
        if (mTopicsSet.contains(topic)) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        (mTopicsSet as HashSet).remove(topic)
                        saveSharedPreferences()
                        Toast.makeText(this, "Desuscrito a $topic", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Error de desuscripción", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }
}
