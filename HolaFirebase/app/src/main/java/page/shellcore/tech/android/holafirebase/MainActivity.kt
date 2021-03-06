package page.shellcore.tech.android.holafirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

const val PATH_START = "start"
const val PATH_MESSAGE = "message"

class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val reference = database.getReference(PATH_START)
        .child(PATH_MESSAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setOnClickListeners()
        setFirebaseEventListener()
    }

    private fun setOnClickListeners() {
        btnSend.setOnClickListener {
            reference.setValue(edtMessage.text.toString().trim())
            edtMessage.setText("")
        }
    }

    private fun setFirebaseEventListener() {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                txtMessage.text = dataSnapshot.getValue(String::class.java)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Error al consultar en Firebase",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
