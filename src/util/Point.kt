package com.github.jflc.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Radius of the earth in km
const val R: Double = 6371.01

/**
 * Geographical point composed by latitude and longitude.
 */
data class Point(val lat: Double, val lng: Double)

fun Point.distance(p: Point): Double {
    val lat1: Double = this.lat
    val lng1: Double = this.lng

    val lat2: Double = p.lat
    val lng2: Double = p.lng

    val latDiff: Double = Math.toRadians(lat2 - lat1)
    val lngDiff: Double = Math.toRadians(lng2 - lng1)

    val a: Double = sin(latDiff/2) * sin(latDiff/2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(lngDiff/2) * sin(lngDiff/2.0)

    var c: Double = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c
}