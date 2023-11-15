package com.saleem.radeef.passenger.ui.rides

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.databinding.ItemRideBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class RideAdapter(val context: Context) : RecyclerView.Adapter<RideAdapter.DriverRideViewHolder>() {

    private var list: MutableList<Ride> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverRideViewHolder {
        val itemView = ItemRideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DriverRideViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DriverRideViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<Ride>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size


    inner class DriverRideViewHolder(val binding: ItemRideBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Ride) {
            val geocoder = Geocoder(context, Locale.getDefault())
//            val pickup = geocoder.getFromLocation(
//                item.pickupLocation.latitude,
//                item.pickupLocation.longitude,
//                1
//            )?.firstOrNull()?.getAddressLine(0) ?: ""
//
//            val destination = geocoder.getFromLocation(
//                item.destination.latitude,
//                item.destination.longitude,
//                1
//            )?.firstOrNull()?.getAddressLine(0) ?: ""

            val coroutineScope = CoroutineScope(Dispatchers.Main)
            var pickup = ""
            var destination = ""
            coroutineScope.launch {
                pickup =
                    getPickupAddress(item.pickupLocation.latitude, item.pickupLocation.longitude)
                destination =
                    getPickupAddress(item.destination.latitude, item.destination.longitude)
                binding.pickupTv.text = pickup
                binding.destinationTv.text = destination
            }



        }

        private suspend fun getPickupAddress(latitude: Double, longitude: Double): String {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            return addresses?.firstOrNull()?.featureName ?: ""
        }
    }
}