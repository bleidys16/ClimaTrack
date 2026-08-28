package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.databinding.ItemTechnicianHistoryBinding
import com.example.climatrack.models.ActividadTecnico
import java.text.SimpleDateFormat
import java.util.*

class TechnicianHistoryAdapter(private val history: List<ActividadTecnico>) :
    RecyclerView.Adapter<TechnicianHistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemTechnicianHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(history[position])
    }

    override fun getItemCount(): Int = history.size

    inner class HistoryViewHolder(private val binding: ItemTechnicianHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ActividadTecnico) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
                val date = inputFormat.parse(item.fecha)
                binding.tvHistoryDate.text = if (date != null) outputFormat.format(date).replaceFirstChar { it.uppercase() } else item.fecha
            } catch (e: Exception) {
                binding.tvHistoryDate.text = item.fecha
            }

            binding.tvHistoryDay.text = "Actividad Registrada"
            binding.tvHistoryStart.text = "Inicio: ${item.horaInicio ?: "--:--"}"
            binding.tvHistoryEnd.text = "Fin: ${item.horaFin ?: "--:--"}"
        }
    }
}
