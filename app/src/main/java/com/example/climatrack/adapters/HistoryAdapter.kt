package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.R
import com.example.climatrack.databinding.ItemHistoryBinding
import com.example.climatrack.models.MantenimientoInfo

class HistoryAdapter(private var list: List<MantenimientoInfo>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<MantenimientoInfo>) {
        list = newList
        notifyDataSetChanged()
    }

    class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(m: MantenimientoInfo) {
            binding.tvHistDate.text = m.fecha
            binding.tvHistDiagnosis.text = m.diagnostico
            binding.tvHistWork.text = "Trabajo: ${m.trabajoRealizado}"
            binding.tvHistType.text = m.tipoServicio

            val context = binding.root.context
            val color = when (m.tipoServicio.uppercase()) {
                "PREVENTIVO" -> R.color.status_finished
                "CORRECTIVO" -> R.color.status_pending
                else -> R.color.status_in_progress
            }
            binding.tvHistType.setTextColor(ContextCompat.getColor(context, color))
        }
    }
}
