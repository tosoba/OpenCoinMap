package com.trm.opencoinmap.feature.venues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.feature.venues.databinding.ItemVenueBinding

internal class VenuesAdapter(private val onItemClick: (Venue) -> Unit) :
  PagingDataAdapter<VenueListItem, VenuesAdapter.ViewHolder>(
    diffCallback =
      object : DiffUtil.ItemCallback<VenueListItem>() {
        override fun areItemsTheSame(oldItem: VenueListItem, newItem: VenueListItem): Boolean =
          oldItem.venue.id == newItem.venue.id

        override fun areContentsTheSame(oldItem: VenueListItem, newItem: VenueListItem): Boolean =
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
    fun bind(item: VenueListItem) {
      with(binding) {
        venueLetterIcon.letter = item.venue.name.first().toString()
        venueNameTextView.text = item.venue.name
        venueCategoryTextView.text = item.venue.category
        item.distanceMeters?.let {
          venueDistanceTextView.text =
            if (it > 1_000.0) "${String.format("%.1f", it / 1_000.0)} km\naway"
            else "${String.format("%.0f", it)} m\naway"
        }
        root.setOnClickListener { onItemClick(item.venue) }
      }
    }
  }
}
