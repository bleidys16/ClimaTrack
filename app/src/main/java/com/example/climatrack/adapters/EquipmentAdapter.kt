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
            val color = when (equipment.estado) {
                "OPERATIVO" -> R.color.status_finished
                "EN MANTENIMIENTO" -> R.color.status_in_progress
                "FUERA DE SERVICIO" -> R.color.status_canceled
                else -> R.color.secondary
            }
            binding.tvEquipStatus.setTextColor(ContextCompat.getColor(context, color))

            binding.root.setOnClickListener {
                onItemClick(equipment)
            }
        }
    }
}
