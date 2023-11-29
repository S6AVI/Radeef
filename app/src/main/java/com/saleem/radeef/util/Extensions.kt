package com.saleem.radeef.util

import android.app.Activity
import android.content.Context
import android.location.Location
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.saleem.radeef.passenger.ui.map.TAG
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun View.hide(){
    visibility = View.GONE
}

fun View.show(){
    visibility = View.VISIBLE
}

fun View.disable() {
    isEnabled = false
}

fun View.enable() {
    isEnabled = true
}

fun Fragment.toast(msg: String?){
    Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
}

fun Any.logD(message: String) {
    Log.d(TAG, message)
}

fun String.isValidPlateNumber(): Boolean {
    val pattern = Regex("[A-Z]{3}[0-9]{4}")
    return pattern.matches(this)
}

fun Double.formatCost(): String {
    val roundedValue = String.format("%.1f", this)
    return "$roundedValue SAR"
}

fun Double.formatDistance(): String {
    val roundedValue = String.format("%.2f", this)
    return "$roundedValue KM"
}

fun Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun LatLng.toGeoPoint(): GeoPoint {
    return GeoPoint(this.latitude, this.longitude)
}
fun LatLng.isDefault(): Boolean {
    logD("location lat: ${this.latitude.toInt()}")
    return this.latitude.toInt() == 0 && this.longitude.toInt() == 0
}

fun Double.toKm() = this / 1000.0

fun Date.formatDate(): String {
    val format = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", Locale.getDefault())
    return format.format(this)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

