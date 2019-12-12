package page.shellcore.tech.android.misfotogrtafas

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.decodeBitmap
import com.google.firebase.database.DatabaseReference
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
    }

    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference

    private lateinit var mCurrentPhotoPath: String
    private lateinit var mPhotoSelectedUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_GALLERY -> setImageFromGallery(data)
                RC_CAMERA -> {}
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
            txtTitle.text = getString(R.string.main_message_question_upload)
        }
    }
}
