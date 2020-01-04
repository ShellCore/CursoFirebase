package page.shellcore.tech.android.multilogin

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RC_SING_IN = 123
    }
    private val mFirebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val mAuthStateListener: FirebaseAuth.AuthStateListener by lazy {
        FirebaseAuth.AuthStateListener {
            val user: FirebaseUser? = it.currentUser
            if (user != null) {
                // TODO
            } else {
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setTosUrl("http://www.google.com")
                        .setAvailableProviders(arrayListOf(AuthUI.IdpConfig.EmailBuilder().build()))
                        .build(),
                    RC_SING_IN
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SING_IN) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bienvenido...", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Algo falló, intente nuevamente", Toast.LENGTH_SHORT)
                    .show()
            }
        }
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
