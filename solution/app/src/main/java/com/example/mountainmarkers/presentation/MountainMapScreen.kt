package com.example.mountainmarkers.presentation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.mountainmarkers.BottomNav
import com.example.mountainmarkers.MarkerType
import com.example.mountainmarkers.MountainMapTopBar
import com.example.mountainmarkers.presentation.common.BigSpinner
import com.example.mountainmarkers.ui.theme.MountainMarkersTheme

@Composable
fun MountainMapScreen(
    viewModel: MountainsViewModel,
) {
//    // Remember the type of marker we want to show
//    var selectedMarkerType by rememberSaveable {
//        mutableStateOf(MarkerType.Basic)
//    }
//
//    Scaffold(
//        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
//        topBar = {
//            MountainMapTopBar(
//                topBarTitleStringRes = selectedMarkerType.title,
//                showAllChecked = showAllChecked,
//                onZoomAllClick = {
//                    viewModel.onEvent(MountainsViewModelEvent.OnZoomAll)
//                },
//                onToggleShowAllClick = {
//                    viewModel.onEvent(MountainsViewModelEvent.OnToggleAllPeaks)
//                }
//            )
//        },
//        bottomBar = { BottomNav(selectedMarkerType) { selectedMarkerType = it } }
//    ) { paddingValues ->
//        when (viewState) {
//            MountainsScreenViewState.Loading -> BigSpinner(paddingValues)
//
//            is MountainsScreenViewState.MountainList -> {
//                MountainMap(
//                    paddingValues,
//                    viewState,
//                    viewModel.getEventChannel(),
//                    selectedMarkerType,
//                    onCameraChange = {
//                        viewModel.onEvent(
//                            MountainsViewModelEvent.OnCameraChange(it)
//                        )
//                    }
//                )
//            }
//        }
//    }
}