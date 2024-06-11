package com.liamxsage.energeticstorage.serialization

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.liamxsage.energeticstorage.extensions.getLogger
import org.bukkit.inventory.ItemStack

class ItemStackAdapter : TypeAdapter<ItemStack>() {

    override fun write(output: JsonWriter, itemStack: ItemStack?) {
        if (itemStack == null) {
            output.nullValue()
            return
        }
        output.beginObject()
        output.jsonValue(ItemStackConverter.itemStackToBase64(itemStack))
        output.endObject()
        getLogger().info("ItemStackAdapter: Wrote ItemStack (${itemStack.type})")
    }

    override fun read(input: JsonReader): ItemStack {
        if (input.peek() == null) {
            input.nextNull()
            return ItemStack.empty()
        }
        input.beginObject()
        val itemStack = ItemStackConverter.itemStackFromBase64(input.nextString())
        input.endObject()
        getLogger().info("ItemStackAdapter: Read ItemStack (${itemStack.type})")
        return itemStack
    }
}