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

            if (!item.horaInicio.isNullOrEmpty() && !item.horaFin.isNullOrEmpty()) {
                try {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val start = sdf.parse(item.horaInicio)
                    val end = sdf.parse(item.horaFin)
                    if (start != null && end != null) {
                        val diff = end.time - start.time
                        val minutes = diff / (1000 * 60)
                        if (minutes > 0) {
                            val h = minutes / 60
                            val m = minutes % 60
                            binding.tvDuration.visibility = android.view.View.VISIBLE
                            binding.tvDuration.text = "Duración: ${h}h ${m}m"
                        } else {
                            binding.tvDuration.visibility = android.view.View.GONE
                        }
                    }
                } catch (e: Exception) {
                    binding.tvDuration.visibility = android.view.View.GONE
                }
            } else {
                binding.tvDuration.visibility = android.view.View.GONE
            }
        }
    }
}
