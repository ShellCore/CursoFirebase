package page.shellcore.tech.android.menucomidas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import page.shellcore.tech.android.menucomidas.dummy.DummyContent

class ItemListActivity : AppCompatActivity() {

    companion object {
        const val PATH_FOOD = "food"
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
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }
}
