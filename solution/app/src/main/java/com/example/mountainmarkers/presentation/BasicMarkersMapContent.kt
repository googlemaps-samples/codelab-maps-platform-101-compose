// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.mountainmarkers.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.mountainmarkers.R
import com.example.mountainmarkers.data.local.Mountain
import com.example.mountainmarkers.data.local.is14er
import com.example.mountainmarkers.data.utils.m
import com.example.mountainmarkers.presentation.utils.BitmapParameters
import com.example.mountainmarkers.data.utils.toElevationString
import com.example.mountainmarkers.presentation.utils.vectorToBitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

/**
 * [GoogleMapComposable] which renders a [MountainList] as a set of basic [Marker]s
 */
@Composable
@GoogleMapComposable
fun BasicMarkersMapContent(
    mountains: List<Mountain>,
    onMountainClick: (Marker) -> Boolean = { false },
    styleMarkers: Boolean
) {
    // Create mountainIcon and fourteenerIcon
    val mountainIcon = vectorToBitmap(
        LocalContext.current,
        BitmapParameters(
            id = R.drawable.baseline_filter_hdr_24,
            iconColor = MaterialTheme.colorScheme.secondary.toArgb(),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer.toArgb(),
        )
    )

    val fourteenerIcon = vectorToBitmap(
        LocalContext.current,
        BitmapParameters(
            id = R.drawable.baseline_filter_hdr_24,
            iconColor = MaterialTheme.colorScheme.onPrimary.toArgb(),
            backgroundColor = MaterialTheme.colorScheme.primary.toArgb(),
        )
    )

    mountains.forEach { mountain ->
        val icon = if (mountain.is14er()) fourteenerIcon else mountainIcon
        Marker(
            state = rememberMarkerState(position = mountain.location),
            title = mountain.name,
            snippet = mountain.elevation.toElevationString(),
            tag = mountain,
            anchor = Offset(0.5f, 0.5f),
            icon = if (styleMarkers) icon else null,
            onClick = { marker ->
                onMountainClick(marker)
                false
            },
            zIndex = if (mountain.is14er()) 5f else 2f
        )
    }
}

//@Preview
//@Composable
//fun BasicMarkersMapContentPreview() {
//    var id = 0
//
//    val mountains = listOf(
//        Mountain(
//            id = id++,
//            "Mount Everest",
//            LatLng(39.1178, -106.4454),
//            4401.2.m,
//        ),
//        Mountain(
//            id = id++,
//            "Uncompahgre Peak",
//            LatLng(38.0717,-107.4621),
//            4365.0.m,
//        ),
//        Mountain(
//            id = id++,
//            "Handies Peak",
//            LatLng(37.913, -107.5044),
//            4284.8.m,
//        )
//    )
//
//    BasicMarkersMapContent(
//        mountains = mountains,
//        styleMarkers = true,
//    )
//}