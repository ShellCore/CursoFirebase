package page.shellcore.tech.android.inventario.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_product.view.*
import page.shellcore.tech.android.inventario.R
import page.shellcore.tech.android.inventario.model.pojo.Product
import page.shellcore.tech.android.inventario.ui.loadImage

class ProductsAdapter : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    private val products = arrayListOf<Product>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(products[position])

    override fun getItemCount() = products.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imgProduct = view.imgProduct
        private val txtProductName = view.txtProductName
        private val txtProductQuantity = view.txtProductQuantity

        fun bind(product: Product) {
            imgProduct.loadImage(product.photoUrl)
            txtProductName.text = product.name
            txtProductQuantity.text = "(${product.quantity})"
        }
    }
}