package com.baf1.hexmaptestandroidview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.baf1.hexmaptestandroidview.cells.CellFieldProvider
import com.baf1.hexmaptestandroidview.cells.CellsUtils
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

@Composable
fun AroundMap(
    cellFieldProvider: CellFieldProvider,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            MapLibre.getInstance(context)
            MapView(context).apply {
                onCreate(null)

                getMapAsync { map ->
                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                        CellsUtils.createCells(
                            style = style,
                            cellFieldProvider = cellFieldProvider,
                        )
                    }
                    map.easeCamera(
                        CameraUpdateFactory.newCameraPosition(
                            cameraPosition = map.cameraPosition.copy(
                                zoom = 12.1,
                                target = LatLng(latitude = 56.8519, longitude = 60.6122)
                            )
                        ),
                        300,
                    )
                }
            }
        },
    )
}
