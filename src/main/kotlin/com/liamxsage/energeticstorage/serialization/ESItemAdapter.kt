package com.liamxsage.energeticstorage.serialization

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.liamxsage.energeticstorage.model.ESItem
import org.bukkit.inventory.ItemStack

class ESItemAdapter : TypeAdapter<ESItem>() {

    override fun write(output: JsonWriter, esItem: ESItem?) {
        if (esItem == null) {
            output.nullValue()
            return
        }
        output.beginObject()
        output.name("amount").value(esItem.amount)
        output.name("itemStack").value(ItemStackConverter.itemStackToBase64(esItem.itemStackAsSingle))
        output.endObject()
    }

    override fun read(input: JsonReader): ESItem {
        var amount: Long = 0
        var itemStack: ItemStack? = null

        input.beginObject()
        while (input.hasNext()) {
            when (input.peek()) {
                JsonToken.NAME -> {
                    when (input.nextName()) {
                        "amount" -> amount = input.nextLong()
                        "itemStack" -> itemStack = ItemStackConverter.itemStackFromBase64(input.nextString())
                        else -> input.skipValue()
                    }
                }
                else -> input.skipValue()
            }
        }
        input.endObject()
        return ESItem(itemStack ?: ItemStack.empty(), amount)
    }
}
