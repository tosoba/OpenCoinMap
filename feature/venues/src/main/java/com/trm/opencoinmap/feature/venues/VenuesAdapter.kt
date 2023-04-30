package com.trm.opencoinmap.feature.venues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.feature.venues.databinding.ItemVenueBinding

internal class VenuesAdapter(private val onItemClick: (Venue) -> Unit) :
  PagingDataAdapter<Venue, VenuesAdapter.ViewHolder>(
    diffCallback =
      object : DiffUtil.ItemCallback<Venue>() {
        override fun areItemsTheSame(oldItem: Venue, newItem: Venue): Boolean =
          oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Venue, newItem: Venue): Boolean =
          oldItem == newItem
      }
  ) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(ItemVenueBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    getItem(position)?.let(holder::bind)
  }

  inner class ViewHolder(
    private val binding: ItemVenueBinding,
  ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(venue: Venue) {
      with(binding) {
        venueName.text = venue.name
        root.setOnClickListener { onItemClick(venue) }
      }
    }
  }
}
