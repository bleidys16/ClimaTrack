package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.databinding.ItemHistoryBinding
import com.example.climatrack.models.Mantenimiento

class HistoryAdapter(private var list: List<Mantenimiento>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Mantenimiento>) {
        list = newList
        notifyDataSetChanged()
    }

    class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(m: Mantenimiento) {
            binding.tvHistDate.text = m.fecha
            binding.tvHistDiagnosis.text = "Diagnóstico: ${m.diagnostico}"
            binding.tvHistWork.text = "Trabajo: ${m.trabajoRealizado}"
        }
    }
}
