package com.liamxsage.energeticstorage.serialization

import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class OptionalTypeAdapter : JsonSerializer<Optional<*>>, JsonDeserializer<Optional<*>> {
    override fun serialize(src: Optional<*>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return src.map(context::serialize).orElse(JsonNull.INSTANCE)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Optional<*> {
        return if (json.isJsonNull) Optional.empty<Any>() else Optional.of(
            context.deserialize<Any>(
                json,
                (typeOfT as ParameterizedType).actualTypeArguments[0]
            )
        )
    }
}