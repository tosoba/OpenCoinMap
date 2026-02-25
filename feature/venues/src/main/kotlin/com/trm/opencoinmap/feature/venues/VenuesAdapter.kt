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

  inner class ViewHolder(private val binding: ItemVenueBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: VenueListItem) {
      with(binding) {
        venueLetterIcon.letter = (item.venue.name.firstOrNull() ?: '?').toString()
        venueNameTextView.text = item.venue.name
        venueCategoryTextView.text = item.venue.category
        item.distanceMeters?.let {
          venueDistanceTextView.text =
            binding.root.context.getString(
              if (it > 1_000.0) R.string.venue_distance_kilometers
              else R.string.venue_distance_meters,
              if (it > 1_000.0) it / 1_000.0 else it,
            )
        }
        root.setOnClickListener { onItemClick(item.venue) }
      }
    }
  }
}
