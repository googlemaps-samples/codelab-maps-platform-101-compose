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

import android.content.Context
import android.util.Xml
import com.example.mountainmarkers.R
import com.example.mountainmarkers.data.utils.meters
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

/**
 * Repository for loading the list of mountain peaks.
 */
class MountainsRepository(@ApplicationContext val context: Context) {
  private val _mountains = MutableStateFlow(emptyList<Mountain>())
  val mountains: StateFlow<List<Mountain>> = _mountains
  private var loaded = false

  /**
   * Loads the list of mountains from the list of mountains from the raw resource.
   */
  suspend fun loadMountains(): StateFlow<List<Mountain>> {
    if (!loaded) {
      loaded = true
      _mountains.value = withContext(Dispatchers.IO) {
        context.resources.openRawResource(R.raw.top_peaks).use { inputStream ->
          readMountains(inputStream)
        }
      }
    }
    return mountains
  }

  /**
   * Reads the [Waypoint]s from the given [inputStream] and returns a list of [Mountain]s.
   */
  private fun readMountains(inputStream: InputStream) =
    readWaypoints(inputStream).mapIndexed { index, waypoint ->
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
  private fun readWaypoints(inputStream: InputStream): Sequence<Waypoint> = sequence {
    // We don't use namespaces
    val ns: String? = null
    val parser = Xml.newPullParser()
    parser.setInput(inputStream, null)

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
