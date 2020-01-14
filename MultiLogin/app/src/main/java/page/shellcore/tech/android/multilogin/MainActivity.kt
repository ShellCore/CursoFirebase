package page.shellcore.tech.android.multilogin

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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
                onSignedOutCleanup()
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(arrayListOf(AuthUI.IdpConfig.EmailBuilder().build()))
                        .build(),
                    RC_SING_IN
                )
            }
        }
    }

    private fun onSignedOutCleanup() {
        onSetDataUser("", "", "")
    }

    private fun onSetDataUser(userName: String, email: String, provider: String) {
        txtName.text = userName
        txtEmail.text = email

        val drawableBase: Int = when (provider) {
            PASSWORD_FIREBASE -> R.drawable.ic_firebase
            else -> {
                R.drawable.ic_unknown
    //                provider = UNKNOWN_PROVIDER
            }
        }

        txtProvider.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableBase, 0, 0, 0)
        txtProvider.text = provider
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val info = packageManager.getPackageInfo("page.shellcore.tech.android.multilogin", PackageManager.GET_SIGNATURES)
            info.signatures.forEach {
                val md = MessageDigest.getInstance("SHA")
                md.update(it.toByteArray())
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (ex: PackageManager.NameNotFoundException) {
            ex.printStackTrace()
        } catch (ex: NoSuchAlgorithmException) {
            ex.printStackTrace()
        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.actionSignOut -> {
                AuthUI.getInstance().signOut(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
