package com.saleem.radeef.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

/*
data class to wrap LatLng with its address title
 */
data class RadeefLocation(
    val latLng: LatLng? = null,
    val title: String? = null
) {

    /*
    custom getter to cast LatLng to GeoPoint
     */
    fun toGeoPoint(): GeoPoint {
        return GeoPoint(latLng!!.latitude, latLng.longitude)
    }
}