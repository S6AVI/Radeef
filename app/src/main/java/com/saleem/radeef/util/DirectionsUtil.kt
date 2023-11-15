package com.saleem.radeef.util

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import com.saleem.radeef.R
import com.saleem.radeef.passenger.ui.map.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception


//fun calculateDistance(start: LatLng, end: LatLng, context: GeoApiContext): Double {
//
//
//    CoroutineScope.launch(Dispatchers.IO) {
//        try {
//            Log.d(TAG, "before directions init")
//            val directions = DirectionsApi.newRequest(context)
//            Log.d(TAG, directions.toString())
//            directions.origin(
//                com.google.maps.model.LatLng(
//                    start.latitude,
//                    start.longitude
//                )
//            )
//            Log.d(TAG, "line: 213: ${directions.toString()}")
//            directions
//                .destination(
//                    com.google.maps.model.LatLng(
//                        end.latitude,
//                        end.longitude
//                    )
//                )
//            Log.d(TAG, "line: 221: ${directions.toString()}")
//            directions
//                .mode(TravelMode.DRIVING)
//                .units(Unit.METRIC)
//                .await()
//
//            val distance = result.routes[0].legs.sumOf { it.distance.inMeters } / 1000.0
//            return distance
//
//            //map.animateCamera(cameraUpdate)
//
//
//            Log.d(TAG, "line: 226: ${directions.toString()}")
//
//        } catch (e: Exception) {
//            Log.d(TAG, "here")
//            Log.d(TAG, e.toString())
//            e.printStackTrace()
//            val cause = e.cause
//            if (cause != null) {
//                Log.d(TAG, "here")
//            }
//        }
//    }
//
//}
