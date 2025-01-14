package com.example.mountainmarkers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mountainmarkers.ui.theme.MountainMarkersTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

class BasicMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MountainMarkersTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapContainer(modifier = Modifier.padding(innerPadding).fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun MapContainer(modifier: Modifier) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 5f)
    }

    Box(modifier = modifier) {
        GoogleMap(
            cameraPositionState = cameraPositionState
        ) {
            // Add your content here
        }
    }
}