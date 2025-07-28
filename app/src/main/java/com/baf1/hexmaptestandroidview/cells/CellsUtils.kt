package com.baf1.hexmaptestandroidview.cells

import com.baf1.hexmaptestandroidview.MapConstant
import com.uber.h3core.util.LatLng
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.has
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.switchCase
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.CustomGeometrySource

object CellsUtils {
    fun createCells(
        style: Style,
        cellFieldProvider: CellFieldProvider,
    ) {
        val source = CustomGeometrySource(MapConstant.Styling.ID_SOURCE, cellFieldProvider)

        val lineLayer =
            LineLayer(MapConstant.Styling.ID_LINE_LAYER, MapConstant.Styling.ID_SOURCE)

        style.addSource(source)
        val firstLabelIndex = style.layers.indexOfFirst {
            it.id
                .lowercase()
                .contains("label")
        }
        style.addLayerAt(lineLayer, firstLabelIndex)
    }

    fun shrinkHexVertices(
        vertices: List<LatLng>,
        resolution: Int
    ): List<LatLng> {
        val factor = when (resolution) {
            6 -> 0.97
            7 -> 0.95
            8 -> 0.93
            9 -> 0.91
            10 -> 0.88
            11 -> 0.85
            else -> 1.0
        }
        val center = findCenter(vertices)
        val newVertices = vertices.map { vertex ->
            LatLng(
                center.lat + (vertex.lat - center.lat) * factor,
                center.lng + (vertex.lng - center.lng) * factor
            )
        }
        return newVertices
    }

    private fun findCenter(vertices: List<LatLng>): LatLng {
        val avgX = vertices.sumOf { it.lat } / vertices.size
        val avgY = vertices.sumOf { it.lng } / vertices.size
        return LatLng(avgX, avgY)
    }
}
