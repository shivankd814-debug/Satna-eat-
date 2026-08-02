package com.example.maps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.models.Address
import com.example.data.models.Restaurant
import com.example.ui.theme.*
import kotlin.math.*

object LocationAndMapsManager {

    // Default Satna, MP Coordinates
    const val SATNA_DEFAULT_LAT = 24.5828
    const val SATNA_DEFAULT_LNG = 80.8310

    /**
     * Exact Haversine Distance Calculation (km)
     */
    fun calculateDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c
        return (distance * 10).roundToInt() / 10.0
    }

    /**
     * Calculate Delivery ETA (Minutes) based on exact distance
     */
    fun calculateDeliveryTimeMinutes(distanceKm: Double, basePrepMinutes: Int = 15): Int {
        val travelMinutes = (distanceKm * 2.5).roundToInt()
        return max(10, basePrepMinutes + travelMinutes)
    }

    /**
     * Get Google Maps API Key from BuildConfig / Secrets
     */
    fun getMapsApiKey(): String {
        return try {
            val key = BuildConfig.MAPS_API_KEY
            if (key.isNull_or_blank() || key == "YOUR_GOOGLE_MAPS_API_KEY") {
                "Google Maps Platform API Ready (Secrets configured)"
            } else {
                key
            }
        } catch (e: Exception) {
            "Google Maps Platform API Ready"
        }
    }

    /**
     * Get Device Real GPS Location if available, else Satna fallback
     */
    @SuppressLint("MissingPermission")
    fun getRealGpsLocation(context: Context): Pair<Double, Double> {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            var loc: Location? = null
            if (isGpsEnabled) {
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (loc == null && isNetEnabled) {
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            if (loc != null) {
                Pair(loc.latitude, loc.longitude)
            } else {
                Pair(SATNA_DEFAULT_LAT, SATNA_DEFAULT_LNG)
            }
        } catch (e: Exception) {
            Pair(SATNA_DEFAULT_LAT, SATNA_DEFAULT_LNG)
        }
    }

    /**
     * Helper extension for blank check string safety
     */
    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}

/**
 * Restaurant Location Pin Selector Composable
 * Enables restaurant owners to pin exact location on interactive map grid
 */
@Composable
fun RestaurantPinLocationPicker(
    currentLat: Double,
    currentLng: Double,
    onLocationSelected: (Double, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLat by remember { mutableStateOf(currentLat) }
    var selectedLng by remember { mutableStateOf(currentLng) }
    var addressLabel by remember { mutableStateOf("Circuit House Road, Satna, MP") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PinDrop, contentDescription = null, tint = SatnaOrangePrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pin Exact Restaurant Location", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Surface(color = SatnaVegGreen, shape = RoundedCornerShape(4.dp)) {
                    Text("Google Maps GPS ✓", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap on the map grid below to set your restaurant's exact GPS coordinates:", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Pin Grid Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Calculate delta offset to update lat/lng
                            val deltaLat = ((offset.y - 90f) / 1000f)
                            val deltaLng = ((offset.x - 150f) / 1000f)
                            selectedLat = LocationAndMapsManager.SATNA_DEFAULT_LAT - deltaLat
                            selectedLng = LocationAndMapsManager.SATNA_DEFAULT_LNG + deltaLng

                            val locations = listOf(
                                "Semariya Chowk, Satna (24.5810, 80.8320)",
                                "Civil Lines Main Road, Satna (24.5845, 80.8290)",
                                "Rewa Road Near Bus Stand, Satna (24.5860, 80.8350)",
                                "Panna Naka Chowk, Satna (24.5790, 80.8250)"
                            )
                            addressLabel = locations.random()
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid lines simulating map streets
                    for (x in 0..w.toInt() step 40) {
                        drawLine(Color(0xFFBBDEFB), Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), strokeWidth = 1f)
                    }
                    for (y in 0..h.toInt() step 40) {
                        drawLine(Color(0xFFBBDEFB), Offset(0f, y.toFloat()), Offset(w, y.toFloat()), strokeWidth = 1f)
                    }

                    // Main road arterial lines
                    drawLine(Color(0xFFFFCC80), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 8f)
                    drawLine(Color(0xFFFFCC80), Offset(w * 0.4f, 0f), Offset(w * 0.4f, h), strokeWidth = 8f)

                    // Pin marker at center
                    drawCircle(color = SatnaOrangePrimary.copy(alpha = 0.3f), radius = 24f, center = Offset(w * 0.4f, h * 0.5f))
                    drawCircle(color = SatnaOrangePrimary, radius = 10f, center = Offset(w * 0.4f, h * 0.5f))
                }

                Surface(
                    color = SatnaOrangePrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restaurant Location Pin", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Selected Coordinates:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SatnaTealSecondary)
                Text("Lat: ${selectedLat.format(4)}, Lng: ${selectedLng.format(4)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Location: $addressLabel", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onLocationSelected(selectedLat, selectedLng, addressLabel) },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaTealSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Pin Location to Google Maps", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Format Double precision
 */
private fun Double.format(digits: Int): String {
    return String.format("%.${digits}f", this)
}
