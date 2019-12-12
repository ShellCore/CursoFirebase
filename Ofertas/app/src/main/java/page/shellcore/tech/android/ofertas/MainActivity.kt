package page.shellcore.tech.android.ofertas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSubscribe.setOnClickListener(this)
        btnUnsubscribe.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val topic = resources.getStringArray(R.array.topicsValues)[spnTopics.selectedItemPosition]

        when (view.id) {
            R.id.btnSubscribe -> suscribe(topic)
            R.id.btnUnsubscribe -> unsuscribe(topic)
        }

    }

    private fun suscribe(topic: String?) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Suscrito a $topic", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Error de suscripción", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun unsuscribe(topic: String?) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Desuscrito a $topic", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Error de desuscripción", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}
