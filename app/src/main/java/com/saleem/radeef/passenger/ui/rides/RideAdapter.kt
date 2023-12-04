package com.saleem.radeef.passenger.ui.rides

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saleem.radeef.data.model.Ride
import com.saleem.radeef.databinding.ItemRidePassengerBinding
import com.saleem.radeef.util.formatCost
import com.saleem.radeef.util.formatDate
import com.saleem.radeef.util.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class RideAdapter(val context: Context) : RecyclerView.Adapter<RideAdapter.DriverRideViewHolder>() {

    private var list: MutableList<Ride> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverRideViewHolder {
        val itemView =
            ItemRidePassengerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DriverRideViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DriverRideViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<Ride>) {
        this.list = filterList(list)
        notifyDataSetChanged()
    }



    override fun getItemCount() = list.size


    inner class DriverRideViewHolder(val binding: ItemRidePassengerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Ride) {
            val coroutineScope = CoroutineScope(Dispatchers.Main)
            var pickup: String
            var destination: String
            coroutineScope.launch {
                pickup =
                    getPickupAddress(
                        item.passengerPickupLocation.latitude,
                        item.passengerPickupLocation.longitude
                    )
                destination =
                    getPickupAddress(
                        item.passengerDestination.latitude,
                        item.passengerDestination.longitude
                    )
                binding.apply {
                    pickupTv.text = pickup
                    destinationTv.text = destination
                    driverNameTv.text = item.driverName
                    costTv.text = item.chargeAmount.formatCost()
                    dateTv.text = item.startTime.formatDate()
                    statusTv.text = item.status
                }
            }


        }

        private fun getPickupAddress(latitude: Double, longitude: Double): String {
            return try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.featureName ?: ""
            } catch (e: Exception) {
                logD("error in getPickupAddress: ${e.message}")
                ""
            }
        }
    }
    private fun filterList(list: MutableList<Ride>): MutableList<Ride> {
        return list.filter { ride -> ride.driverId != "" }.toMutableList()
    }
}