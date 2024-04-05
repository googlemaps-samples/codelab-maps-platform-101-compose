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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.combine
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

  // Whether or not to show all of the high peaks
  private var showAllMountains = MutableStateFlow(false)

  val mountainsScreenViewState =
    mountainsRepository.mountains.combine(showAllMountains) { allMountains, showAllMountains ->
      if (allMountains.isEmpty()) {
        MountainsScreenViewState.Loading
      } else {
        val filteredMountains =
          if (showAllMountains) allMountains else allMountains.filter { it.is14er() }
        val boundingBox = filteredMountains.map { it.location }.toLatLngBounds()
        MountainsScreenViewState.MountainList(
          mountains = filteredMountains,
          boundingBox = boundingBox,
          showingAllPeaks = showAllMountains,
        )
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = MountainsScreenViewState.Loading
    )

  init {
    // Load the full set of mountains
    viewModelScope.launch {
      mountainsRepository.loadMountains()
    }
  }

  // Handle user events
  fun onEvent(event: MountainsViewModelEvent) {
    when (event) {
      OnZoomAll -> onZoomAll()
      OnToggleAllPeaks -> toggleAllPeaks()
    }
  }

  private fun onZoomAll() {
    sendScreenEvent(MountainsScreenEvent.OnZoomAll)
  }

  private fun toggleAllPeaks() {
    showAllMountains.value = !showAllMountains.value
  }

  // Send events back to the UI via the event channel
  private fun sendScreenEvent(event: MountainsScreenEvent) {
    viewModelScope.launch { _eventChannel.send(event) }
  }
}
