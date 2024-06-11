package com.liamxsage.energeticstorage.extensions

import dev.fruxz.ascend.tool.time.calendar.Calendar
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


fun <T : Any> T.getLogger(): org.slf4j.Logger {
    return LoggerFactory.getLogger(this::class.java)
}

fun <T : Any> T.nullIf(condition: (T) -> Boolean): T? {
    return if (condition(this)) null else this
}

/**
 * Converts an [Instant] to a [Calendar].
 *
 * This method takes an [Instant] and converts it to a [Calendar] object. The [Instant] represents a point in time,
 * while the [Calendar] represents a date and time in a specific calendar system.
 *
 * @return The [Calendar] representation of the [Instant].
 */
fun Instant.toCalendar() =
    Calendar(GregorianCalendar.from(ZonedDateTime.from(this.atZone(ZoneId.systemDefault()))))