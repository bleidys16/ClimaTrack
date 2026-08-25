package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.databinding.ItemSparePartBinding
import com.example.climatrack.models.DetalleRepuestoInfo
import java.text.NumberFormat
import java.util.*

class SparePartsAdapter(
    private var list: List<DetalleRepuestoInfo>,
    private val onMenuClick: (DetalleRepuestoInfo) -> Unit
) : RecyclerView.Adapter<SparePartsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSparePartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<DetalleRepuestoInfo>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemSparePartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetalleRepuestoInfo) {
            binding.tvPartCode.text = item.repuestoCodigo
            binding.tvPartName.text = item.repuestoNombre
            binding.tvPartQty.text = "Cantidad: ${item.cantidad}"
            binding.tvPartUnit.text = item.repuestoUnidad ?: "Unidad"
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            binding.tvPartPrice.text = formatter.format(item.precio * item.cantidad)

            binding.ivMenuMore.setOnClickListener {
                onMenuClick(item)
            }
        }
    }
}
