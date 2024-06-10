package com.liamxsage.energeticstorage.serialization

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.bukkit.Bukkit
import org.bukkit.Location

class LocationAdapter : TypeAdapter<Location>() {

    override fun write(output: JsonWriter, location: Location?) {
        if (location == null) {
            output.nullValue()
            return
        }
        output.beginObject()
        output.value(convertFromStringLocation(location))
        output.endObject()
    }

    override fun read(input: JsonReader): Location {
        if (input.peek() == null) {
            input.nextNull()
            return Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)
        }
        input.beginObject()
        val location = convertToLocation(input.nextString())
        input.endObject()
        return location
    }


    private fun convertFromStringLocation(location: Location) =
        "${location.world},${location.x},${location.y},${location.z},${location.yaw},${location.pitch}"

    private fun convertToLocation(string: String): Location {
        val split = string.split(",")
        return Location(
            Bukkit.getWorld(split[0]) ?: Bukkit.getWorlds()[0],
            split[1].toDouble(),
            split[2].toDouble(),
            split[3].toDouble(),
            split[4].toFloat(),
            split[5].toFloat()
        )
    }
}