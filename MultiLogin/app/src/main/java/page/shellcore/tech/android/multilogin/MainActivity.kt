package page.shellcore.tech.android.multilogin

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RC_SING_IN = 123
        private const val RC_FROM_GALLERY = 124
        private const val UNKNOWN_PROVIDER: String = "Proveedor desconocido"
        private const val PASSWORD_FIREBASE: String = "password"
        private const val FACEBOOK: String = "facebook.com"
        private const val GOOGLE: String = "google.com"
        private const val PATH_PROFILE = "profile"
        private const val MY_PHOTO_AUTH = "my_photo_auth"
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

                loadImage(user.photoUrl)
            } else {
                onSignedOutCleanup()

                val facebookIdp = AuthUI.IdpConfig.FacebookBuilder()
                    .setPermissions(listOf("user_friends", "user_gender"))
                    .build()

                val googleIdp = AuthUI.IdpConfig.GoogleBuilder()
                    .build()

                val customLayout = AuthMethodPickerLayout.Builder(R.layout.layout_login_custom)
                    .setEmailButtonId(R.id.btnEmail)
                    .setGoogleButtonId(R.id.btnGoogle)
                    .setFacebookButtonId(R.id.btnFacebook)
                    .setTosAndPrivacyPolicyId(R.id.txtPolicy)
                    .build()

                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                            arrayListOf(
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                facebookIdp,
                                googleIdp
                            )
                        )
                        .setLogo(R.mipmap.ic_launcher)
                        .setTosAndPrivacyPolicyUrls(
                            "http://www.google.com",
                            "http://www.google.com"
                        )
                        .setTheme(R.style.GreenTheme)
                        .setAuthMethodPickerLayout(customLayout)
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
            FACEBOOK -> R.drawable.ic_facebook
            GOOGLE -> R.drawable.ic_google
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
            val info = packageManager.getPackageInfo(
                "page.shellcore.tech.android.multilogin",
                PackageManager.GET_SIGNATURES
            )
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

        imgUser.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, RC_FROM_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SING_IN -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bienvenido...", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Algo falló, intente nuevamente", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            RC_FROM_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    progressBar.visibility = View.VISIBLE

                    uploadImageTask(data!!.data)
//                    uploadImageFile(data!!.data)
                }
            }
        }
    }

    private fun uploadImageTask(uri: Uri?) {
        val storage = FirebaseStorage.getInstance()
        val reference = storage.reference
            .child(PATH_PROFILE)
            .child(MY_PHOTO_AUTH)

        var bitmap: Bitmap

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            bitmap = resizeBitmap(bitmap, 1024)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val data = byteArrayOutputStream.toByteArray()

            val uploadTask = reference.putBytes(data)
            uploadTask.addOnProgressListener {
                val progress = (100 * it.bytesTransferred) / it.totalByteCount
                progressBar.progress = progress.toInt()
            }
                .addOnCompleteListener {
                    progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    reference.downloadUrl
                        .addOnSuccessListener { uri ->
                            val user = FirebaseAuth.getInstance()
                                .currentUser
                            if (user != null) {
                                val request = UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
                                    .build()
                                user.updateProfile(request)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            loadImage(user.photoUrl)
                                        }
                                    }
                            }
                        }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error...", Toast.LENGTH_SHORT)
                        .show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            width = (height * bitmapRatio).toInt()
            height = maxSize
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun uploadImageFile(uri: Uri?) {
        val storage = FirebaseStorage.getInstance()
        val reference = storage.reference
            .child(PATH_PROFILE)
            .child(MY_PHOTO_AUTH)
        if (uri != null) {
            reference.putFile(uri)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred) / it.totalByteCount
                    progressBar.progress = progress.toInt()
                }
                .addOnCompleteListener {
                    progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    reference.downloadUrl
                        .addOnSuccessListener { uri ->
                            val user = FirebaseAuth.getInstance()
                                .currentUser
                            if (user != null) {
                                val request = UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
                                    .build()
                                user.updateProfile(request)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            loadImage(user.photoUrl)
                                        }
                                    }
                            }
                        }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error...", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun loadImage(photoUrl: Uri?) {
        val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()

        Glide.with(this)
            .load(photoUrl)
            .apply(options)
            .into(imgUser)
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
        return when (item.itemId) {
            R.id.actionSignOut -> {
                AuthUI.getInstance().signOut(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
