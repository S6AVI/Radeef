package com.saleem.radeef.util

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.saleem.radeef.passenger.ui.map.TAG

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