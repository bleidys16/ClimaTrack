package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.R
import com.example.climatrack.databinding.ItemTechnicianBinding
import com.example.climatrack.models.TecnicoStats

class TechnicianAdapter(
    private var technicians: List<TecnicoStats>,
    private val onItemClick: (TecnicoStats) -> Unit
) : RecyclerView.Adapter<TechnicianAdapter.TechnicianViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechnicianViewHolder {
        val binding = ItemTechnicianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TechnicianViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TechnicianViewHolder, position: Int) {
        holder.bind(technicians[position])
    }

    override fun getItemCount(): Int = technicians.size

    fun updateList(newList: List<TecnicoStats>) {
        technicians = newList
        notifyDataSetChanged()
    }

    inner class TechnicianViewHolder(private val binding: ItemTechnicianBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tech: TecnicoStats) {
            binding.tvTechName.text = tech.nombre
            binding.tvTechStats.text = "${tech.trabajosRealizados} trabajos realizados"
            
            val isActive = tech.isActive == 1
            binding.tvTechStatus.text = if (isActive) "ACTIVO" else "INACTIVO"
            
            val statusColor = if (isActive) R.color.status_finished else R.color.status_pending
            val statusBg = if (isActive) R.color.status_finished_container else R.color.status_pending_container
            
            binding.tvTechStatus.setTextColor(ContextCompat.getColor(binding.root.context, statusColor))
            binding.cardStatusIndicator.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, statusBg))

            binding.root.setOnClickListener { onItemClick(tech) }
        }
    }
}
