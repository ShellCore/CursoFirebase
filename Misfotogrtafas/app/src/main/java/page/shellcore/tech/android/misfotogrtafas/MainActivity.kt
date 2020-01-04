package page.shellcore.tech.android.misfotogrtafas

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.decodeBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_content.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
                RC_CAMERA -> setImageFromCamera(data)
            }
        }
    }

    private fun setImageFromGallery(data: Intent?) {
        if (data != null) {
            mPhotoSelectedUri = data.data!!
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, mPhotoSelectedUri)
            imgPhoto.setImageBitmap(bitmap)
            btnCloseImage.visibility = View.GONE
            txtTitle.setText(R.string.main_message_question_upload)
        }
    }

    private fun setImageFromCamera(data: Intent?) {
//        val extras = data!!.extras
//        val bitmap = extras!!.get("data") as Bitmap
        mPhotoSelectedUri = addPicGallery()
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, mPhotoSelectedUri)
            imgPhoto.setImageBitmap(bitmap)
            btnCloseImage.visibility = View.GONE
            txtTitle.setText(R.string.main_message_question_upload)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun addPicGallery(): Uri {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val file = File(mCurrentPhotoPath)
        val contentUri = Uri.fromFile(file)
        intent.setData(contentUri)
        this.sendBroadcast(intent)
        return contentUri
    }

    private fun setupNavigation() {
        navMain.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gallery -> checkPermissionToApp(Manifest.permission.READ_EXTERNAL_STORAGE, RP_STORAGE)
                R.id.navigation_camera -> checkPermissionToApp(Manifest.permission.CAMERA, RP_CAMERA)
            }
            false
        }
    }

    private fun checkPermissionToApp(permission: String, requestPermission: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestPermission)
                return
            }
        }

        when(requestPermission) {
            RP_STORAGE -> fromGallery()
            RP_CAMERA -> fromCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                RP_STORAGE -> fromGallery()
                RP_CAMERA -> fromCamera()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun fromGallery() {
        txtTitle.setText(R.string.title_gallery)
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }

    // Función para conseguir la imágen miniatura
//    private fun fromCamera() {
//        txtTitle.setText(R.string.title_camera)
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(intent, RC_CAMERA)
//    }

    private fun fromCamera() {
        txtTitle.setText(R.string.title_camera)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile = createImageFile()
            if (photoFile != null) {
                val photoUri = FileProvider.getUriForFile(this, "page.shellcore.tech.android.misfotogrtafas", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, RC_CAMERA)
            }
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.ROOT)
            .format(Date())
        val imageFileName = "MY_PHOTO${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var image: File? = null
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir)
            mCurrentPhotoPath = image.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return image
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
        ).show()
    }
}
