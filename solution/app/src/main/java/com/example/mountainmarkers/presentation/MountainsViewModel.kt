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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountainmarkers.MarkerType
import com.example.mountainmarkers.data.local.MountainsRepository
import com.example.mountainmarkers.data.local.is14er
import com.example.mountainmarkers.domain.mappers.toLatLngBounds
import com.example.mountainmarkers.presentation.MountainsViewModelEvent.OnToggleAllPeaks
import com.example.mountainmarkers.presentation.MountainsViewModelEvent.OnZoomAll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for loading and managing the list of mountains
 */
@HiltViewModel
class MountainsViewModel
@Inject
constructor(
  mountainsRepository: MountainsRepository
) : ViewModel() {
  private val _eventChannel = Channel<MountainsScreenEvent>()

  // Event channel to send events to the UI
  internal fun getEventChannel() = _eventChannel.receiveAsFlow()

  private val _markerType = MutableStateFlow(MarkerType.Basic)
  val markerType = _markerType.asStateFlow()

  val loading = mountainsRepository.loading

  // Whether or not to show all of the high peaks
  private val _showAllMountains = MutableStateFlow(false)
  val showAllMountains = _showAllMountains.asStateFlow()

  private val allMountains = mountainsRepository.mountains.onStart {
    mountainsRepository.loadMountains()
  }.stateIn(
    scope = viewModelScope,
    SharingStarted.Lazily,
    initialValue = emptyList()
  )

  val mountains = allMountains.combine(showAllMountains) { mountains, showAll ->
    Log.w("MountainsViewModel", "Mountains changed: ${mountains.size} ${mountains.firstOrNull()}")

    val filteredMountains = if (showAll) mountains else mountains.filter { it.is14er() }
    val boundingBox = filteredMountains.map { it.location }.toLatLngBounds()
    MountainList(
      mountains = filteredMountains,
      boundingBox = boundingBox,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = MountainList(
      mountains = emptyList(),
      boundingBox = EmptyLatLngBounds,
    )
  )

//    mountainsRepository.mountains.combine(showAllMountains) { allMountains, showAllMountains ->
//      if (allMountains.isEmpty()) {
//        MountainsScreenViewState.Loading
//      } else {
//        val filteredMountains =
//          if (showAllMountains) allMountains else allMountains.filter { it.is14er() }
//        val boundingBox = filteredMountains.map { it.location }.toLatLngBounds()
//        MountainsScreenViewState.MountainList(
//          mountains = filteredMountains,
//          boundingBox = boundingBox,
//          showingAllPeaks = showAllMountains,
//        )
//      }
//    }.stateIn(
//      scope = viewModelScope,
//      started = SharingStarted.WhileSubscribed(5000),
//      initialValue = MountainsScreenViewState.Loading
//    )

  // Handle user events
  fun onEvent(event: MountainsViewModelEvent) {
    when (event) {
      OnZoomAll -> onZoomAll()
      OnToggleAllPeaks -> toggleAllPeaks()
      is MountainsViewModelEvent.OnShowAllMountainsChange -> { _showAllMountains.value = event.showAllMountains }
      is MountainsViewModelEvent.OnCameraChange -> {
        Log.d("Camera changed", event.cameraProjection.visibleRegion.latLngBounds.toString())
      }

      is MountainsViewModelEvent.OnMarkerTypeChange -> {
        _markerType.value = event.markerType
      }
    }
  }

  private fun onZoomAll() {
    sendScreenEvent(MountainsScreenEvent.OnZoomAll)
  }

  private fun toggleAllPeaks() {
    _showAllMountains.value = !showAllMountains.value
  }

  // Send events back to the UI via the event channel
  private fun sendScreenEvent(event: MountainsScreenEvent) {
    viewModelScope.launch { _eventChannel.send(event) }
  }
}
