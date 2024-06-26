package com.saleem.radeef.driver.ui.rides

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saleem.radeef.data.model.Ride
import com.saleem.radeef.databinding.ItemRideBinding
import com.saleem.radeef.util.formatCost
import com.saleem.radeef.util.formatDate
import com.saleem.radeef.util.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class DriverRideAdapter(val context: Context) :
    RecyclerView.Adapter<DriverRideAdapter.RideViewHolder>() {

    private var list: MutableList<Ride> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val itemView = ItemRideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RideViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<Ride>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size


    inner class RideViewHolder(val binding: ItemRideBinding) :
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
                    passengerNameTv.text = item.passengerName
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
                logD("DriverRideAdapter - getPickupAddress - error: ${e.message}")
                ""
            }
        }
    }
}