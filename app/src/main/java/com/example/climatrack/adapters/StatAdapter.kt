package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.databinding.ItemStatBinding

data class StatItem(val name: String, val value: Int)

class StatAdapter(private val items: List<StatItem>) : RecyclerView.Adapter<StatAdapter.StatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val binding = ItemStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount(): Int = items.size

    class StatViewHolder(private val binding: ItemStatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatItem, rank: Int) {
            binding.tvStatRank.text = rank.toString()
            binding.tvStatName.text = item.name
            binding.tvStatValue.text = item.value.toString()
        }
    }
}
