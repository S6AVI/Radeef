import android.location.Geocoder
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import android.content.Context

fun configureMapSettings(map: GoogleMap) {
    map.uiSettings.apply {
        isZoomControlsEnabled = false
        isZoomGesturesEnabled = true
        isRotateGesturesEnabled = true
        isTiltGesturesEnabled = false
        isCompassEnabled = false
        isScrollGesturesEnabled = true
        isMyLocationButtonEnabled = true
    }
}

fun setCameraBoundsAndZoom(map: GoogleMap, bounds: LatLngBounds) {
    map.setLatLngBoundsForCameraTarget(bounds)
    map.setMinZoomPreference(8f)
    map.setMaxZoomPreference(15f)
}

val RIYADH = LatLng(24.7136, 46.6753)

val saudiArabiaBounds = LatLngBounds(
    LatLng(16.0, 34.0), // Southwest corner
    LatLng(33.0, 56.0)  // Northeast corner
)

fun configureLocationButton(mapView: View?) {
    mapView?.let {
        val locationButton =
            (it.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
                Integer.parseInt("2")
            )
        val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)
    }
}

private fun getLatLngFromAddress(address: String, context: Context): LatLng {
    val geocoder = Geocoder(context)
    val results = geocoder.getFromLocationName(address, 1)
    val location = results?.get(0)!!
    return LatLng(location.latitude, location.longitude)
}
