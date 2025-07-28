package com.baf1.hexmaptestandroidview

import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.geojson.Point

fun LatLngBounds.isCross(points: List<com.uber.h3core.util.LatLng>): Boolean {
    val h3Points = points.map { LatLng(it.lat, it.lng) }

    if (h3Points.any { this.contains(it) }) return true

    val boundsCorners = listOf(
        southWest,
        LatLng(southWest.latitude, northWest.longitude),
        northEast,
        LatLng(northEast.latitude, southWest.longitude)
    )

    return boundsCorners.any { pointInsidePolygon(it, h3Points) }
}

private fun pointInsidePolygon(
    point: LatLng,
    polygon: List<LatLng>
): Boolean {
    var result = false
    var j = polygon.lastIndex

    for (i in polygon.indices) {
        val pi = polygon[i]
        val pj = polygon[j]

        if ((pi.longitude > point.longitude) != (pj.longitude > point.longitude)) {
            val intersection = (pj.latitude - pi.latitude) *
                    (point.longitude - pi.longitude) /
                    (pj.longitude - pi.longitude) + pi.latitude
            if (point.latitude < intersection) {
                result = !result
            }
        }
        j = i
    }
    return result
}


fun com.uber.h3core.util.LatLng.toMapLibrePoint(): Point = Point.fromLngLat(lng, lat)


fun CameraPosition.copy(
    target: LatLng? = null,
    zoom: Double? = null,
    bearing: Double? = null,
    tilt: Double? = null
): CameraPosition {
    return CameraPosition
        .Builder()
        .target(target ?: this.target)
        .zoom(zoom ?: this.zoom)
        .bearing(bearing ?: this.bearing)
        .tilt(tilt ?: this.tilt)
        .build()
}