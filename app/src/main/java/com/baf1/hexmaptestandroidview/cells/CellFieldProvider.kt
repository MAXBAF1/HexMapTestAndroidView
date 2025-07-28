package com.baf1.hexmaptestandroidview.cells

import com.baf1.hexmaptestandroidview.CellDTO
import com.baf1.hexmaptestandroidview.Teams
import com.baf1.hexmaptestandroidview.isCross
import com.baf1.hexmaptestandroidview.toMapLibrePoint
import com.uber.h3core.H3Core
import com.uber.h3core.util.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.style.sources.GeometryTileProvider
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Polygon
import java.util.concurrent.ConcurrentHashMap

class CellFieldProvider(private val h3: H3Core) : GeometryTileProvider {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val totalCells = ConcurrentHashMap<Int, ConcurrentHashMap<String, TeamsCells>>()

    override fun getFeaturesForBounds( bounds: LatLngBounds, zoomLevel: Int): FeatureCollection {
        val features = mutableListOf<Feature>()
        val resolution = getFieldResolution(zoomLevel)
        val resCells = totalCells.getOrPut(resolution) { ConcurrentHashMap() }

        if (shouldUseParentResolution(resolution)) {
            processParentResolution(bounds, resolution, resCells, features)
        } else {
            processCurrentResolution(bounds, resolution, resCells, features)
        }

        return FeatureCollection.fromFeatures(features)
    }

    private fun shouldUseParentResolution(resolution: Int): Boolean {
        return resolution - 2 >= 6
    }

    private fun processParentResolution(
        bounds: LatLngBounds,
        resolution: Int,
        resCells: ConcurrentHashMap<String, TeamsCells>,
        features: MutableList<Feature>
    ) {
        val parentResCells = totalCells.getOrPut(resolution - 2) { ConcurrentHashMap() }

        parentResCells.keys.forEach { parentId ->
            if (!bounds.isCross(h3.cellToBoundary(parentId))) return@forEach

            h3.cellToChildren(parentId, resolution).forEach { childId ->
                addFeatureIfInBounds(bounds, childId, resolution, resCells, features)
            }
        }
    }

    private fun processCurrentResolution(
        bounds: LatLngBounds,
        resolution: Int,
        resCells: ConcurrentHashMap<String, TeamsCells>,
        features: MutableList<Feature>
    ) {
        resCells.keys.forEach { id ->
            addFeatureIfInBounds(bounds, id, resolution, resCells, features)
        }
    }

    private fun addFeatureIfInBounds(
        bounds: LatLngBounds,
        cellId: String,
        resolution: Int,
        resCells: ConcurrentHashMap<String, TeamsCells>,
        features: MutableList<Feature>
    ) {
        val latLngs = h3.cellToBoundary(cellId)
        val shrinkLatLngs = CellsUtils.shrinkHexVertices(latLngs, resolution)
        if (!bounds.isCross(latLngs)) return
        val feature = transformToFeature(
            shrinkLatLngs + shrinkLatLngs.first(),
            resCells.getOrPut(cellId) { TeamsCells() }.dominantTeam
        )
        features.add(feature)
    }


    private fun transformToFeature(
        latLngs: List<LatLng>,
        team: Teams,
    ): Feature {
        val points = latLngs.map { it.toMapLibrePoint() }
        val newFeature = Feature.fromGeometry(Polygon.fromLngLats(listOf(points)))
        if (team != Teams.NONE) newFeature.addNumberProperty(TEAM_ID, team.id)

        return newFeature
    }

    fun addCells(
        cells: List<CellDTO>,
    ) {
        coroutineScope.launch {
            runBlocking {
                cells.chunked(40_000).map { chunk ->
                    async(Dispatchers.Default) {
                        chunk.forEach { processCell(it) }
                    }
                }.awaitAll()
            }
        }
    }

    private fun processCell(cell: CellDTO) {
        val initialResolution = h3.getResolution(cell.id)
        var resolution = initialResolution
        var lastResCell = cell

        while (resolution in 6..11) {
            val resMap = totalCells.getOrPut(resolution) { ConcurrentHashMap() }

            when (initialResolution) {
                6 -> {
                    processChildren(resMap, cell.id, resolution)
                    resolution++
                }

                11 -> {
                    lastResCell = processParent(resMap, lastResCell, resolution) ?: break
                    resolution--
                }
            }
        }
    }

    private fun processChildren(
        resMap: ConcurrentHashMap<String, TeamsCells>,
        cellId: String,
        resolution: Int,
    ) {
        h3.cellToChildren(cellId, resolution).forEach { resMap.getOrPut(it) { TeamsCells() } }
    }

    private fun processParent(
        resMap: ConcurrentHashMap<String, TeamsCells>,
        cell: CellDTO,
        resolution: Int,
    ): CellDTO? {
        val parentCellId = h3.cellToParentAddress(cell.id, resolution)
        val teamsCells = resMap.getOrPut(parentCellId) { TeamsCells() }

        val oldDominantTeam = teamsCells.dominantTeam
        teamsCells.updateTeamsCells(cell)
        val newDominantTeam = teamsCells.dominantTeam

        return if (oldDominantTeam != newDominantTeam) {
            val newCell = CellDTO(parentCellId, newDominantTeam.id)
            resMap[parentCellId] = teamsCells
            newCell
        } else null
    }

    private fun getFieldResolution(zoomLevel: Int): Int = when {
        zoomLevel < 9 -> 6
        zoomLevel < 10 -> 7
        zoomLevel < 11 -> 8
        zoomLevel < 12 -> 9
        zoomLevel < 13 -> 10
        else -> 11
    }

    companion object {
        const val TEAM_ID = "teamId"
    }
}