package page.shellcore.tech.android.misfotogrtafas

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference

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
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
