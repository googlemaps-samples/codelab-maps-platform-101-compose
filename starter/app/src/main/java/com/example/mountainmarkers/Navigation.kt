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

package com.example.mountainmarkers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * An enumeration of the different marker types to demonstrate.
 */
enum class MarkerType(
    val title: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Basic(
        title = R.string.basic_markers_label,
        selectedIcon = Icons.Filled.Place,
        unselectedIcon = Icons.Outlined.Place,
    ),
    Advanced(
        title = R.string.advanced_markers_label,
        selectedIcon = Icons.Filled.Place,
        unselectedIcon = Icons.Outlined.Place,

        ),
    Clustered(
        title = R.string.clustered_markers_label,
        selectedIcon = Icons.Filled.Place,
        unselectedIcon = Icons.Outlined.Place,
    )
}

/**
 * A composable to render an application top bar with a title, zoom all button, and a switch to
 * toggle between seeing all of the mountains or just a subset.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MountainMapTopBar(
    topBarTitleStringRes: Int,
    showAllChecked: Boolean,
    onZoomAllClick: () -> Unit,
    onToggleShowAllClick: (Boolean) -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(id = topBarTitleStringRes)) },
        actions = {
            IconButton(
                onClick = onZoomAllClick
            ) {
                Icon(
                    painterResource(id = R.drawable.baseline_zoom_out_map_24),
                    contentDescription = stringResource(id = R.string.zoom_all)
                )
            }
            SwitchWithText(
                label = stringResource(id = R.string.show_all),
                checked = showAllChecked,
                onCheckedChange = onToggleShowAllClick
            )
        }
    )
}

/**
 * A switch with a text label.  Clicking either the switch or its label will call the
 * [onCheckedChange] callback.
 */
@Composable
private fun SwitchWithText(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Text(text = label)
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}

/**
 * A bottom navigation composable to select the kind of markers to show.
 */
@Composable
fun BottomNav(selectedScreen: MarkerType, onMarkerTypeClicked: (MarkerType) -> Unit) {
    NavigationBar {
        MarkerType.entries.forEach { markerType ->
            val selected = selectedScreen == markerType

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onMarkerTypeClicked(markerType)
                },
                label = {
                    Text(text = stringResource(id = markerType.title))
                },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) markerType.selectedIcon else markerType.unselectedIcon,
                        contentDescription = stringResource(id = markerType.title)
                    )
                }
            )
        }
    }
}
