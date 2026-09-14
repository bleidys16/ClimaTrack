package com.example.climatrack.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.databinding.ItemEvidenceBinding
import com.example.climatrack.models.Evidencia
import java.io.File

class EvidenceAdapter(
    private var list: List<Evidencia>,
    private val onDeleteClick: (Evidencia) -> Unit
) : RecyclerView.Adapter<EvidenceAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemEvidenceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(evidencia: Evidencia) {
            val file = File(evidencia.rutaFoto)
            if (file.exists()) {
                binding.imgEvidence.setImageURI(Uri.fromFile(file))
            }
            binding.tvDate.text = evidencia.fecha
            binding.btnDelete.setOnClickListener { onDeleteClick(evidencia) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEvidenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Evidencia>) {
        list = newList
        notifyDataSetChanged()
    }
}
