package com.saleem.radeef.passenger.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.saleem.radeef.R
import com.saleem.radeef.databinding.ItemResultBinding

class SearchResultAdapter(
    private val listener: OnItemClickListener,
    private val results: List<AutocompletePrediction>) :
    RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = ItemResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return SearchResultViewHolder(view)
    }

    override fun getItemCount() = results.size

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val item = results[position]
        holder.bind(item)

    }

    inner class SearchResultViewHolder(val binding: ItemResultBinding) : RecyclerView.ViewHolder(binding.root) {


        init {
            binding.apply {
                root.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val item = results.get(position)
                        listener.onItemClick(item)
                    }
                }
            }
        }

        fun bind(item: AutocompletePrediction) {
            binding.apply {
                resultName.text = item.getFullText(null)
                val distance = item.distanceMeters?.div(1000.0) ?: 0.0
                resultDistance.text = String.format("%.1f Km", distance)
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(item: AutocompletePrediction)
    }

}