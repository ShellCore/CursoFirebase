package page.shellcore.tech.android.inventario.model.pojo

import com.google.firebase.database.Exclude

data class Product(

    @Exclude
    var id: Long,
    var name: String,
    var quantity: Int,
    var photoUrl: String
) {

    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val QUANTITY = "quantity"
        const val PHOTO_URL = "photo_url"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}