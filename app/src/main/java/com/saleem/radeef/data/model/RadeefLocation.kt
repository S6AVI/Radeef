package com.saleem.radeef.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

data class RadeefLocation(
    val latLng: LatLng? = null,
    val title: String? = null
) {
    fun toGeoPoint(): GeoPoint {
        return GeoPoint(latLng!!.latitude, latLng.longitude)
    }

    companion object {
        fun convertToRadeefLocation(geoLocation: GeoPoint, title: String): RadeefLocation {
            return RadeefLocation(
                latLng = LatLng(geoLocation.latitude, geoLocation.longitude),
                title = title
            )
        }
    }
}