package com.saleem.radeef.ui.rides

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.databinding.ItemRideBinding

class RideAdapter: RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

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


    inner class RideViewHolder(val binding: ItemRideBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Ride){
            binding.pickupTv.setText(item.pickupLocation.toString())
            binding.destinationTv.setText(item.destination.toString())

        }
    }
}