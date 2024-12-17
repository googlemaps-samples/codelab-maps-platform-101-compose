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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mountainmarkers.data.utils.ImperialUnitsConverter
import com.example.mountainmarkers.data.utils.LocalUnitsConverter
import com.example.mountainmarkers.data.utils.MetricUnitsConverter
import com.example.mountainmarkers.presentation.MountainMapScreen
import com.example.mountainmarkers.presentation.MountainsScreenViewState.Loading
import com.example.mountainmarkers.presentation.MountainsScreenViewState.MountainList
import com.example.mountainmarkers.presentation.MountainsViewModel
import com.example.mountainmarkers.presentation.common.BigSpinner
import com.example.mountainmarkers.ui.theme.MountainMarkersTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MountainsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val screenViewState by viewModel.mountainsScreenViewState.collectAsStateWithLifecycle()

            val unitsConverter = if (LocalConfiguration.current.locales.get(0).country == "US") {
                ImperialUnitsConverter
            } else {
                MetricUnitsConverter
            }

            CompositionLocalProvider(
                LocalUnitsConverter provides unitsConverter
            ) {
                MountainMarkersTheme(
                    dynamicColor = false
                ) {

                    when (screenViewState) {
                        Loading -> LoadingScreen()
                        is MountainList -> MountainMapScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BigSpinner(paddingValues)
        }
    }
}
