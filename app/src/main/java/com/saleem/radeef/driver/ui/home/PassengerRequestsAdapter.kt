package com.saleem.radeef.driver.ui.home

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.databinding.ItemPassengerRequestBinding
import com.saleem.radeef.util.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PassengerRequestsAdapter(val context: Context) :
    ListAdapter<RideWithDistance, PassengerRequestsAdapter.PassengerRequestsViewHolder>(
        object : DiffUtil.ItemCallback<RideWithDistance>() {

            override fun areItemsTheSame(
                oldItem: RideWithDistance,
                newItem: RideWithDistance
            ): Boolean {
                logD("adapter - are items the same: old:$oldItem\nnew:$newItem")
                return oldItem.ride.rideID == newItem.ride.rideID
            }

            override fun areContentsTheSame(
                oldItem: RideWithDistance,
                newItem: RideWithDistance
            ): Boolean {
                logD("adapter - are items the same: old:$oldItem\nnew:$newItem")
                return oldItem.ride.rideID == newItem.ride.rideID
            }
        }
    ) {


    private var list: MutableList<RideWithDistance> = arrayListOf()

    //var getDistance: ((LatLng, LatLng) -> Double)? = null
    var getCost: ((Double) -> Double)? = null
    //var getPassengerName: ((String) -> String)? = null


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        logD("adapter attached")
    }

    override fun onCurrentListChanged(
        previousList: MutableList<RideWithDistance>,
        currentList: MutableList<RideWithDistance>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        logD("list changed")
        logD("previous: $previousList")
        logD("current: $currentList")
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassengerRequestsViewHolder {
        logD("adapter: onCreate")
        val itemView = ItemPassengerRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PassengerRequestsViewHolder(itemView)
    }

    override fun getItemCount() = list.size


    override fun onBindViewHolder(holder: PassengerRequestsAdapter.PassengerRequestsViewHolder, position: Int) {
        logD("adapter: onBind")
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<RideWithDistance>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class PassengerRequestsViewHolder(val binding: ItemPassengerRequestBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RideWithDistance) {
            // Fetch data and perform calculations outside of the bind function
            logD("inside bind")
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            coroutineScope.launch {
                val ride = item.ride
                val pickup = getPickupAddress(ride.passengerPickupLocation.latitude, ride.passengerPickupLocation.longitude)
                val destination = getPickupAddress(ride.passengerDestination.latitude, ride.passengerDestination.longitude)
                val distance = item.distance
                val cost = getCost?.invoke(distance).toString()
                val passengerName = ride.passengerName


                withContext(Dispatchers.Main) {
                    // Update the UI with the retrieved and calculated data
                    binding.pickupTextView.text = pickup
                    binding.destinationTextView.text = destination
                    binding.distanceTextView.text = distance.toString()
                    binding.costTextView.text = getCost?.invoke(distance).toString()
                    binding.passengerNameTextView.text = passengerName
                }
            }
        }
    }

//    private class RideDiffUtil: DiffUtil.ItemCallback<Pair<Ride, LatLng>>() {
//        override fun areItemsTheSame(
//            oldItem: Pair<Ride, LatLng>,
//            newItem: Pair<Ride, LatLng>
//        ): Boolean {
//            return oldItem.first.rideID == newItem.first.rideID
//        }
//
//        override fun areContentsTheSame(
//            oldItem: Pair<Ride, LatLng>,
//            newItem: Pair<Ride, LatLng>
//        ): Boolean {
//            return oldItem.first.rideID == newItem.first.rideID
//        }
//    }



    private fun getPickupAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses?.firstOrNull()?.featureName ?: ""
    }


}

data class RideWithDistance(val ride: Ride, val distance: Double)