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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mountainmarkers.R
import com.example.mountainmarkers.data.local.Mountain
import com.example.mountainmarkers.data.local.is14er
import com.example.mountainmarkers.data.utils.LocalUnitsConverter
import com.example.mountainmarkers.presentation.MountainsScreenViewState.MountainList
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering

data class IconColor(val iconColor: Color, val backgroundColor: Color, val borderColor: Color)

data class MountainClusterItem(
    val mountain: Mountain,
    val snippetString: String
) : ClusterItem {
    override fun getPosition() = mountain.location
    override fun getTitle() = mountain.name
    override fun getSnippet() = snippetString
    override fun getZIndex() = 0f
}

/**
 * [GoogleMapComposable] which renders a [MountainList] using the [Clustering] composable
 */
@OptIn(MapsComposeExperimentalApi::class)
@Composable
@GoogleMapComposable
fun ClusteringMarkersMapContent(
    mountains: List<Mountain>,
    onClusterClick: (Cluster<out ClusterItem>) -> Boolean = { false },
    onMountainClick: (ClusterItem) -> Boolean = { false },
) {
    val unitsConverter = LocalUnitsConverter.current
    val resources = LocalContext.current.resources

    val backgroundAlpha = 0.6f

    val fourteenerColors = IconColor(
        iconColor = MaterialTheme.colorScheme.onPrimary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha),
        borderColor = MaterialTheme.colorScheme.primary
    )

    val otherColors = IconColor(
        iconColor = MaterialTheme.colorScheme.secondary,
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = backgroundAlpha),
        borderColor = MaterialTheme.colorScheme.secondary
    )

    val mountainClusterItems by remember(mountains) {
        mutableStateOf(
            mountains.map { mountain ->
                MountainClusterItem(
                    mountain = mountain,
                    snippetString = unitsConverter.toElevationString(resources, mountain.elevation)
                )
            }
        )
    }

    Clustering(
        items = mountainClusterItems,
        onClusterClick = { onClusterClick(it) },
        onClusterItemClick = {
            onMountainClick(it)
            false
        },
        onClusterItemInfoWindowClick = {
        },
        clusterContent = null,
        clusterItemContent = { mountainItem ->
            val colors = if (mountainItem.mountain.is14er()) {
                fourteenerColors
            } else {
                otherColors
            }
            SingleMountain(colors)
        },
    )
}

@Composable
private fun SingleMountain(
    colors: IconColor,
) {
    Icon(
        painterResource(id = R.drawable.baseline_filter_hdr_24),
        tint = colors.iconColor,
        contentDescription = "",
        modifier = Modifier
            .size(32.dp)
            .padding(1.dp)
            .drawBehind {
                drawCircle(color = colors.backgroundColor, style = Fill)
                drawCircle(color = colors.borderColor, style = Stroke(width = 3f))
            }
            .padding(4.dp)
    )
}
