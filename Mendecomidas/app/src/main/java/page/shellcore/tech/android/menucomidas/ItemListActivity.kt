package page.shellcore.tech.android.menucomidas

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import page.shellcore.tech.android.menucomidas.dummy.DummyContent

class ItemListActivity : AppCompatActivity() {

    companion object {
        const val PATH_FOOD = "food"
        const val PATH_CODE = "code"
        const val PATH_PROFILE = "profile"
    }

    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (item_detail_container != null) {
            twoPane = true
        }

        setupRecyclerView(item_list)
        setupOnClickListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_info -> {
                showInfoDialog()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showInfoDialog() {
        val txtCode = createTextInfoCode()

        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference(PATH_PROFILE).child(PATH_CODE)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                txtCode.text = dataSnapshot.getValue(String::class.java)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@ItemListActivity,
                    "No se puede cargar el código",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        val builder = AlertDialog.Builder(this)
            .setTitle("Mi código")
            .setPositiveButton("Ok", null)

        builder.setView(txtCode)
        builder.show()
    }

    private fun createTextInfoCode(): TextView {
        val txtCode = TextView(this)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        txtCode.layoutParams = layoutParams
        txtCode.gravity = Gravity.CENTER
        txtCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        return txtCode
    }

    private fun setupOnClickListeners() {
        btnSave.setOnClickListener {
            saveNewFood()
        }
    }

    private fun saveNewFood() {
        val comida = obtenerComida()
        persisteComida(comida)
        limpiaCamposComida()
    }

    private fun limpiaCamposComida() {
        edtName.setText("")
        edtPrice.setText("")
    }

    private fun persisteComida(comida: DummyContent.Comida) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference(PATH_FOOD)
        reference.push().setValue(comida)
    }

    private fun obtenerComida(): DummyContent.Comida {
        return DummyContent.Comida(
            nombre = edtName.text.toString().trim(),
            precio = edtPrice.text.toString().trim()
        )
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, twoPane)

        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference(PATH_FOOD)

        reference.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@ItemListActivity, "Cancelled", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Toast.makeText(this@ItemListActivity, "Moved", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val item = dataSnapshot.getValue(DummyContent.Comida::class.java)
                item!!.id = dataSnapshot.key.toString()

                if (DummyContent.ITEMS.contains(item)) {
                    DummyContent.updateItem(item)
                }

                recyclerView.adapter!!.notifyDataSetChanged()
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val item = dataSnapshot.getValue(DummyContent.Comida::class.java)
                item!!.id = dataSnapshot.key.toString()

                if (!DummyContent.ITEMS.contains(item)) {
                    DummyContent.addItem(item)
                }

                recyclerView.adapter!!.notifyDataSetChanged()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val item = dataSnapshot.getValue(DummyContent.Comida::class.java)
                item!!.id = dataSnapshot.key.toString()

                if (DummyContent.ITEMS.contains(item)) {
                    DummyContent.deleteItem(item)
                }

                recyclerView.adapter!!.notifyDataSetChanged()
            }

        })
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity,
        private val values: List<DummyContent.Comida>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as DummyContent.Comida
                if (twoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(ItemDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = "$${item.precio}"
            holder.contentView.text = item.nombre

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }

            holder.btnDelete.setOnClickListener {
                val database = FirebaseDatabase.getInstance()
                val reference = database.getReference(PATH_FOOD)
                reference.child(item.id)
                    .removeValue()
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
            val btnDelete = view.btnDelete
        }
    }
}
