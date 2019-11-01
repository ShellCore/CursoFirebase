package page.shellcore.tech.android.menucomidas.dummy

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample nombre for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent {

    val ITEMS: MutableList<Comida> = ArrayList()

    val ITEM_MAP: MutableMap<String, Comida> = HashMap()

    private const val COUNT = 0

    init {
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    fun addItem(item: Comida) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    fun updateItem(item: Comida) {
        ITEMS.set(ITEMS.indexOf(item), item)
        ITEM_MAP.put(item.id, item)
    }

    fun deleteItem(item: Comida) {
        ITEMS.remove(item)
        ITEM_MAP.remove(item.id)
    }

    private fun createDummyItem(position: Int): Comida {
        return Comida(position.toString(), "Item " + position, makeDetails(position))
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore precio information here.")
        }
        return builder.toString()
    }

    /**
     * A item representing a piece of comida.
     */
    data class Comida(var id: String = "", var nombre: String = "", var precio: String = "") {
        override fun toString(): String = nombre

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Comida

            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }


    }
}
