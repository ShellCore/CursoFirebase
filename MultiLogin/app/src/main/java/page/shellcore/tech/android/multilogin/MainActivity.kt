package page.shellcore.tech.android.multilogin

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RC_SING_IN = 123
        private const val UNKNOWN_PROVIDER: String = "Proveedor desconocido"
        private const val PASSWORD_FIREBASE: String = "password"
    }

    private val mFirebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val mAuthStateListener: FirebaseAuth.AuthStateListener by lazy {
        FirebaseAuth.AuthStateListener {
            val user: FirebaseUser? = it.currentUser
            if (user != null) {
                onSetDataUser(
                    user.displayName!!,
                    user.email!!,
                    if (user.providerData.isNotEmpty()) user.providerData[1].providerId else UNKNOWN_PROVIDER
                )
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

    private fun onSetDataUser(userName: String, email: String, provider: String) {
        txtName.text = userName
        txtEmail.text = email
        var provider = provider

        val drawableBase: Int

        when (provider) {
            PASSWORD_FIREBASE -> drawableBase = R.drawable.ic_firebase
            else -> {
                drawableBase = R.drawable.ic_unknown
//                provider = UNKNOWN_PROVIDER
            }
        }

        txtProvider.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableBase, 0, 0, 0)
        txtProvider.text = provider
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
