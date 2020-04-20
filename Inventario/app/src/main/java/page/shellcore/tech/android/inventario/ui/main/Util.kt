package page.shellcore.tech.android.inventario.ui

import android.widget.ImageView
import com.bumptech.glide.Glide
import page.shellcore.tech.android.inventario.R

fun ImageView.loadImage(url: String) {
    Glide.with(this.context)
        .load(url)
        .error(R.mipmap.ic_launcher)
        .centerCrop()
        .into(this)
}