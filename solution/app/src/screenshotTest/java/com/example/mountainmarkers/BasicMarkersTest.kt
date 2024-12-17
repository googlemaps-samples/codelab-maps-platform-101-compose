package com.example.mountainmarkers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.example.mountainmarkers.data.utils.ImperialUnitsConverter
import com.example.mountainmarkers.data.utils.LocalUnitsConverter

class BasicMarkersTest {
    @Preview
    @Composable
    fun BasicMarkersPreview_null_island() {
        CompositionLocalProvider(
            LocalUnitsConverter provides ImperialUnitsConverter
        ) {

        }
    }
}