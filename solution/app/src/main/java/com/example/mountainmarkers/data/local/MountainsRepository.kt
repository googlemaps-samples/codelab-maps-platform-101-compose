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

package com.example.mountainmarkers.data.local

import android.content.res.AssetManager
import android.util.Log
import android.util.Xml
import com.example.mountainmarkers.data.utils.meters
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.Closeable
import java.io.Reader

/**
 * Repository for loading the list of mountain peaks.
 */
class MountainsRepository(
  private val assetManager: AssetManager,
  private val coroutineScope: CoroutineScope
) : Closeable {
  private val _mountains = MutableStateFlow(emptyList<Mountain>())
  val mountains = _mountains.asStateFlow()

  private val _loading = MutableStateFlow(false)
  val loading = _loading.asStateFlow()

  private val mutex = Mutex()

  /**
   * Loads the list of mountains from the list of mountains from the raw resource.
   */
  suspend fun loadMountains() {
    // Use the mutex to ensure the mountains are loaded only once
    mutex.withLock {
      if (_mountains.value.isEmpty()) {
        _loading.value = true
        try {
          _mountains.value = withContext(Dispatchers.IO) {
            println("Loading mountains from assets")
            assetManager.open("top_peaks.gpx").bufferedReader().use {
              println("reading mountains")
              readMountains(it)
            }
          }
        } catch (e: IOException) {
          Log.e("MountainsRepository", "Error loading mountains")
        } finally {
          _loading.value = false
        }
      }
    }
  }

  /**
   * Reads the [Waypoint]s from the given [inputStream] and returns a list of [Mountain]s.
   */
  private fun readMountains(input: Reader) =
    readWaypoints(input).mapIndexed { index, waypoint ->
      waypoint.toMountain(index)
    }.toList()

  /**
   * Class for collecting data fields and creating a valid [Waypoint] or null if any required data
   * is invalid or missing.
   */
  private data class WaypointBuilder(
    var latitude: Double? = null,
    var longitude: Double? = null,
    var name: String? = null,
    var elevation: Double? = null,
  ) {
    fun build(): Waypoint? {
      val latitude = latitude ?: return null
      val longitude = longitude ?: return null
      val elevation = elevation ?: return null
      val name = name ?: return null

      return Waypoint(
        name = name,
        location = LatLng(latitude, longitude),
        elevationMeters = elevation,
      )
    }
  }

  /**
   * Read all of the waypoints from a GPX file.
   */
  private fun readWaypoints(input: Reader): Sequence<Waypoint> = sequence {
    // We don't use namespaces
    val ns: String? = null
    val parser = Xml.newPullParser()
    parser.setInput(input)

    try {
      var eventType = parser.eventType

      var builder: WaypointBuilder? = null

      while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
          XmlPullParser.START_TAG -> {
            if (parser.name == "wpt") {
              builder = WaypointBuilder(
                latitude = parser.getAttributeValue(ns, "lat").toDouble(),
                longitude = parser.getAttributeValue(ns, "lon").toDouble(),
              )
            } else if (builder != null) {
              when (parser.name) {
                "name" -> builder.name = parser.nextText()
                "ele" -> builder.elevation = parser.nextText().toDouble()
              }
            }
          }

          XmlPullParser.END_TAG -> {
            if (parser.name == "wpt" && builder != null) {
              builder.build()?.let { waypoint -> yield(waypoint) }
              builder = null
            }
          }
        }
        eventType = parser.next()
      }

    } catch (e: XmlPullParserException) {
      // Handle parsing errors
    } catch (e: IOException) {
      // Handle IO errors
    }
  }

  override fun close() {
    // Cancel any ongoing coroutines or release resources here if needed
    coroutineScope.cancel()
  }
}

private data class Waypoint(
  val name: String,
  val location: LatLng,
  val elevationMeters: Double,
)

private fun Waypoint.toMountain(id: Int): Mountain {
  return Mountain(
    id = id,
    name = name,
    location = location,
    elevation = elevationMeters.meters
  )
}
