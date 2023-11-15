package com.saleem.radeef.driver.ui.home

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.databinding.ItemPassengerRequestBinding
import com.saleem.radeef.databinding.ItemRideBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PassengerRequestsAdapter(val context: Context): RecyclerView.Adapter<PassengerRequestsAdapter.PassengerRequestsViewHolder>() {

    private var list: MutableList<Ride> = arrayListOf()

    var getDistance: ((LatLng, LatLng) -> Double)? = null
    var getCost: ((LatLng, LatLng) -> Double)? = null
    //var getPassengerName: ((String) -> String)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassengerRequestsViewHolder {
        val itemView = ItemPassengerRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PassengerRequestsViewHolder(itemView)
    }

    override fun getItemCount() = list.size


    override fun onBindViewHolder(holder: PassengerRequestsAdapter.PassengerRequestsViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<Ride>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class PassengerRequestsViewHolder(val binding: ItemPassengerRequestBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Ride) {
            // Fetch data and perform calculations outside of the bind function
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                val pickup = getPickupAddress(item.pickupLocation.latitude, item.pickupLocation.longitude)
                val destination = getPickupAddress(item.destination.latitude, item.destination.longitude)
                val distance = getDistance?.invoke(item.pickup, item.dist).toString()
                val cost = getCost?.invoke(item.pickup, item.dist).toString()
                val passengerName = item.passengerName


                withContext(Dispatchers.Main) {
                    // Update the UI with the retrieved and calculated data
                    binding.pickupTextView.text = pickup
                    binding.destinationTextView.text = destination
                    binding.distanceTextView.text = distance
                    binding.costTextView.text = cost
                    binding.passengerNameTextView.text = passengerName
                }
            }
        }
    }

    private suspend fun getPickupAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses?.firstOrNull()?.featureName ?: ""
    }


}