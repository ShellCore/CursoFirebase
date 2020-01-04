package page.shellcore.tech.android.multilogin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private val mFirebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val mAuthStateListener: FirebaseAuth.AuthStateListener by lazy {
        FirebaseAuth.AuthStateListener {
            val user: FirebaseUser? = it.currentUser
            if (user != null) {
                // TODO
            } else {
                // TODO
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener { mAuthStateListener }
    }
}
