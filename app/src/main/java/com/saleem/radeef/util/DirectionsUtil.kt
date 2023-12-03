package com.saleem.radeef.util


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
