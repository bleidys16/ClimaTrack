package com.example.climatrack.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.R
import com.example.climatrack.databinding.ItemEquipmentBinding
import com.example.climatrack.models.Equipo
import java.io.File

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

            // Photo loading
            try {
                if (equipment.imagenPath != null) {
                    val file = File(equipment.imagenPath)
                    if (file.exists()) {
                        binding.ivEquipmentPhoto.setImageURI(Uri.fromFile(file))
                        binding.ivEquipmentPhoto.clearColorFilter()
                        binding.ivEquipmentPhoto.alpha = 1.0f
                    } else {
                        setDefaultIcon()
                    }
                } else {
                    setDefaultIcon()
                }
            } catch (e: Exception) {
                setDefaultIcon()
            }

            val context = binding.root.context
            val (containerColor, textColor) = when (equipment.estado) {
                "OPERATIVO" -> R.color.status_finished_container to R.color.status_finished
                "EN MANTENIMIENTO" -> R.color.status_in_progress_container to R.color.status_in_progress
                "FUERA DE SERVICIO" -> R.color.status_error_container to R.color.status_error
                else -> R.color.status_pending_container to R.color.status_pending
            }
            binding.tvEquipStatus.backgroundTintList = ContextCompat.getColorStateList(context, containerColor)
            binding.tvEquipStatus.setTextColor(ContextCompat.getColor(context, textColor))

            binding.root.setOnClickListener {
                onItemClick(equipment)
            }
        }

        private fun setDefaultIcon() {
            binding.ivEquipmentPhoto.setImageResource(R.drawable.ic_nav_equipment)
            binding.ivEquipmentPhoto.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.ube))
            binding.ivEquipmentPhoto.alpha = 0.5f
        }
    }
}
