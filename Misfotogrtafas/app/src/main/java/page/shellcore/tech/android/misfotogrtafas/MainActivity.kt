package page.shellcore.tech.android.misfotogrtafas

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_content.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val RC_GALLERY = 21
        const val RC_CAMERA = 22

        const val RP_CAMERA = 121
        const val RP_STORAGE = 122

        const val IMAGE_DIRECTORY = "/MyPhotoApp"
        const val MY_PHOTO = "my_photo"

        const val PATH_PROFILE = "profile"
        const val PATH_PHOTO_URL = "photoUrl"

        val options = RequestOptions().centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    private val mStorageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    private val mDatabaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance()
            .reference
            .child(PATH_PROFILE)
            .child(PATH_PHOTO_URL)
    }

    private lateinit var mCurrentPhotoPath: String
    private lateinit var mPhotoSelectedUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
        setupOnClicks()
        configPhotoProfile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_GALLERY -> setImageFromGallery(data)
                RC_CAMERA -> {}
            }
        }
    }

    private fun setupNavigation() {
        navMain.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gallery -> fromGallery()
                R.id.navigation_camera -> txtTitle.setText(R.string.title_camera)
            }
            false
        }
    }

    private fun fromGallery() {
        txtTitle.setText(R.string.title_gallery)
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }

    private fun setupOnClicks() {
        btnUpload.setOnClickListener {
            uploadPhoto()
        }
        btnCloseImage.setOnClickListener {
            deletePhoto()
        }
    }

    private fun uploadPhoto() {
        val profileReference = mStorageReference.child(PATH_PROFILE)
        val photoReference = profileReference.child(MY_PHOTO)
        photoReference.putFile(mPhotoSelectedUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                photoReference.downloadUrl
            }.addOnCompleteListener {
                if (it.isSuccessful) {
                    showMessage(R.string.main_message_upload_success)
                    val downloadUri: Uri = it.result!!
                    savePhotoUrl(downloadUri)
                    btnCloseImage.visibility = View.VISIBLE
                    txtTitle.text = getString(R.string.main_message_done)
                } else {
                    showMessage(R.string.main_message_upload_error)
                }
            }
    }

    private fun savePhotoUrl(downloadUri: Uri) {
        mDatabaseReference.setValue(downloadUri.toString())
    }

    private fun deletePhoto() {
        mStorageReference.child(PATH_PROFILE)
            .child(MY_PHOTO)
            .delete()
            .addOnSuccessListener {
                mDatabaseReference.removeValue()
                showMessage(R.string.main_message_delete_success)
                imgPhoto.setImageBitmap(null)
                btnCloseImage.visibility = View.GONE
            }.addOnFailureListener {
                showMessage(R.string.main_message_delete_error)
            }
    }

    private fun configPhotoProfile() {
        // Recuperación de imagen desde Firebase Storage
        /*mStorageReference.child(PATH_PROFILE)
            .child(MY_PHOTO)
            .downloadUrl
            .addOnSuccessListener {
                loadImage(it)
                btnCloseImage.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                btnCloseImage.visibility = View.GONE
                showMessage(R.string.main_message_error_notFound)
            }*/

        // Recuperación de imagen desde Firebase Database
        mDatabaseReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                loadImage(dataSnapshot.value as String)
                btnCloseImage.visibility = View.VISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                btnCloseImage.visibility = View.GONE
                showMessage(R.string.main_message_error_notFound)
            }

        })
    }

    private fun setImageFromGallery(data: Intent?) {
        if (data != null) {
            mPhotoSelectedUri = data.data!!
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, mPhotoSelectedUri)
            loadImage(bitmap)
            btnCloseImage.visibility = View.GONE
            txtTitle.text = getString(R.string.main_message_question_upload)
        }
    }

    private fun loadImage(image: Any) {
        Glide.with(this)
            .load(image)
            .apply(options)
            .into(imgPhoto)
    }

    private fun showMessage(messageId: Int) {
        Snackbar.make(
            container,
            getString(messageId),
            Snackbar.LENGTH_LONG
        )
            .show()
    }
}
