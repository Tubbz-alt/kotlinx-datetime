/*
 * Copyright 2016-2020 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */
/* Based on the ThreeTenBp project.
 * Copyright (c) 2007-present, Stephen Colebourne & Michael Nascimento Santos
 */

package kotlinx.datetime

internal class ZonedDateTime(val dateTime: LocalDateTime, private val zone: TimeZone, val offset: ZoneOffset) {
    internal fun plusYears(years: Long): ZonedDateTime = resolve(dateTime.plusYears(years))
    internal fun plusMonths(months: Long): ZonedDateTime = resolve(dateTime.plusMonths(months))
    internal fun plusDays(days: Long): ZonedDateTime = resolve(dateTime.plusDays(days))

    private fun resolve(dateTime: LocalDateTime): ZonedDateTime = with(zone) { dateTime.atZone(offset) }

    override fun equals(other: Any?): Boolean =
        this === other || other is ZonedDateTime &&
            dateTime == other.dateTime && offset == other.offset && zone == other.zone

    @OptIn(ExperimentalStdlibApi::class)
    override fun hashCode(): Int {
        return dateTime.hashCode() xor offset.hashCode() xor zone.hashCode().rotateLeft(3)
    }

    override fun toString(): String {
        var str = dateTime.toString() + offset.toString()
        if (offset !== zone) {
            str += "[$zone]"
        }
        return str
    }
}

// org.threeten.bp.ZonedDateTime#until
// This version is simplified and to be used ONLY in case you know the timezones are equal!
internal fun ZonedDateTime.until(other: ZonedDateTime, unit: CalendarUnit): Long =
    when (unit) {
        // if the time unit is date-based, the offsets are disregarded and only the dates and times are compared.
        CalendarUnit.YEAR, CalendarUnit.MONTH, CalendarUnit.WEEK, CalendarUnit.DAY -> {
            dateTime.until(other.dateTime, unit)
        }
        // if the time unit is not date-based, we need to make sure that [other] is at the same offset as [this].
        CalendarUnit.HOUR, CalendarUnit.MINUTE, CalendarUnit.SECOND, CalendarUnit.NANOSECOND -> {
            val offsetDiff = offset.totalSeconds - other.offset.totalSeconds
            dateTime.until(other.dateTime.plusSeconds(offsetDiff.toLong()), unit)
        }
    }