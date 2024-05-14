package com.example.platform.location.current

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.platform.base.PermissionBox
import com.google.android.catalog.framework.annotations.Sample
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Sample(
    name = "位置 - 获取周边商店",
    description = "本示例展示如何请求周边商店位置",
    documentation = "https://developers.google.com/places/android-sdk",
)
@Composable
fun NearbyStoresScreen() {
    val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    PermissionBox(
        permissions = permissions,
        requiredPermissions = listOf(permissions.first()),
        onGranted = {
            NearbyStoresContent(
                usePreciseLocation = it.contains(Manifest.permission.ACCESS_FINE_LOCATION),
            )
        },
    )
}

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun NearbyStoresContent(usePreciseLocation: Boolean) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val placesClient = remember {
        Places.initialize(context, "YOUR_API_KEY")
        Places.createClient(context)
    }
    var locationInfo by remember {
        mutableStateOf("")
    }
    var nearbyStores by remember {
        mutableStateOf(listOf<String>())
    }

    Column(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val result = locationClient.lastLocation.await()
                    if (result != null) {
                        val placeFields = listOf(
                            Place.Field.NAME,
                            Place.Field.LAT_LNG
                        )
                        val request = FindCurrentPlaceRequest.newInstance(placeFields)
                        val response = placesClient.findCurrentPlace(request).await()
                        val places = response.placeLikelihoods.map { it.place }
                        nearbyStores = places.map { "${it.name}: ${it.latLng}" }
                        locationInfo = "获取到的周边商店：\n" + nearbyStores.joinToString("\n")
                    } else {
                        locationInfo = "没有已知的最后位置。请尝试首先获取当前位置"
                    }
                    Log.d("NearbyStoresScreen", "Nearby stores: $nearbyStores")
                }
            },
        ) {
            Text("获取周边商店位置")
        }

        Text(
            text = locationInfo,
        )
    }
}
