package com.baf1.hexmaptestandroidview

import androidx.compose.ui.unit.dp

object MapConstant {
    object Styling {
        const val ID_SOURCE: String = "ID_SOURCE"
        const val ID_LINE_LAYER: String = "ID_LINE_LAYER"
        const val ID_LOCATION_LAYER: String = "ID_LOCATION_LAYER"

        const val ID_FILL_LAYER: String = "ID_FILL_LAYER"
        const val ID_EVENTS_LAYER: String = "ID_EVENTS_LAYER"
        const val ID_EVENTS_SOURCE: String = "ID_EVENTS_SOURCE"
        const val ID_CLUSTER_LAYER: String = "ID_CLUSTER_LAYER"
        const val ID_EVENT_ICON: String = "ID_EVENT_ICON_IMAGE"
        const val ID_EVENT_CLUSTER_ICON: String = "ID_EVENT_CLUSTER_ICON"
        const val ID_EVENT_ID: String = "ID_EVENT_ID"

         val PAINTED_CELL_STROKE_WIDTH = 1.5.dp
         val UNPAINTED_CELL_STROKE_WIDTH = 0.4.dp
        const val HEX_BG_ALPHA = 0.3F
        const val MARKER_SIZE = 100
        const val EVENT_BORDER = 6f
        const val EVENT_CLUSTER_RADIUS = 30
        const val EVENT_CLUSTER_TEXT_SIZE = 18F

        const val MARKER_MIN_ZOOM_LEVEL: Double = 7.0
    }

    const val MAX_H3_RESOLUTION = 11
    const val MIN_H3_RESOLUTION = 6
    const val DEFAULT_ZOOM_LEVEL: Double = 12.1

    const val ZOOM_LEVEL_DELTA = 1
    val INIT_POINT = 56.838011 to 60.597465

    val fieldResolutions = MIN_H3_RESOLUTION..MAX_H3_RESOLUTION
    fun getFieldResolution(zoomLevel: Int): Int = when {
        zoomLevel < 9 -> 6
        zoomLevel < 10 -> 7
        zoomLevel < 11 -> 8
        zoomLevel < 12 -> 9
        zoomLevel < 13 -> 10
        else -> 11
    }
}