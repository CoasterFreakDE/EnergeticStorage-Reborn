package com.liamxsage.energeticstorage.serialization

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*

class UUIDAdapter : TypeAdapter<UUID>() {

    override fun write(output: JsonWriter, uuid: UUID?) {
        if (uuid == null) {
            output.nullValue()
            return
        }
        output.beginObject()
        output.value(uuid.toString())
        output.endObject()
    }

    override fun read(input: JsonReader): UUID {
        if (input.peek() == null) {
            input.nextNull()
            return UUID.randomUUID()
        }
        input.beginObject()
        val uuid = UUID.fromString(input.nextString())
        input.endObject()
        return uuid
    }


}