package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.R
import com.example.climatrack.databinding.ItemEquipmentBinding
import com.example.climatrack.models.Equipo

class EquipmentAdapter(
    private var equipmentList: List<Equipo>,
    private val onItemClick: (Equipo) -> Unit
) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = ItemEquipmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        holder.bind(equipmentList[position])
    }

    override fun getItemCount(): Int = equipmentList.size

    fun updateList(newList: List<Equipo>) {
        equipmentList = newList
        notifyDataSetChanged()
    }

    inner class EquipmentViewHolder(private val binding: ItemEquipmentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(equipment: Equipo) {
            binding.tvEquipCode.text = equipment.codigo
            binding.tvEquipType.text = equipment.tipo
            binding.tvEquipBrandModel.text = "${equipment.marca} ${equipment.modelo}"
            binding.tvEquipStatus.text = equipment.estado

            val context = binding.root.context
            val (containerColor, textColor) = when (equipment.estado) {
                "OPERATIVO" -> R.color.status_finished_container to R.color.status_finished
                "EN MANTENIMIENTO" -> R.color.status_in_progress_container to R.color.status_in_progress
                "FUERA DE SERVICIO" -> R.color.status_error_container to R.color.status_error
                else -> R.color.md_theme_light_surfaceVariant to R.color.text_secondary
            }
            binding.tvEquipStatus.backgroundTintList = ContextCompat.getColorStateList(context, containerColor)
            binding.tvEquipStatus.setTextColor(ContextCompat.getColor(context, textColor))

            binding.root.setOnClickListener {
                onItemClick(equipment)
            }
        }
    }
}
