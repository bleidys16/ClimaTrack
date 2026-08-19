package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.R
import com.example.climatrack.databinding.ItemOrderBinding
import com.example.climatrack.models.OrdenInfo

class OrdersAdapter(
    private var orders: List<OrdenInfo>,
    private val onItemClick: (OrdenInfo) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateList(newList: List<OrdenInfo>) {
        orders = newList
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrdenInfo) {
            binding.tvOrderNum.text = order.numero
            binding.tvClientName.text = "Cliente: ${order.clienteNombre}"
            binding.tvEquipmentInfo.text = "Equipo: ${order.equipoNombre}"
            binding.tvDate.text = order.fecha
            binding.tvServiceType.text = order.tipoServicio
            binding.tvStatus.text = order.estado

            // Color del estado
            val context = binding.root.context
            val (bg, color) = when (order.estado) {
                "PENDIENTE" -> R.drawable.bg_status_pending to R.color.white
                "EN PROCESO" -> R.drawable.bg_status_in_progress to R.color.white
                "FINALIZADA" -> R.drawable.bg_status_finished to R.color.white
                "CANCELADA" -> R.drawable.bg_status_canceled to R.color.white
                else -> R.drawable.bg_status_pending to R.color.white
            }
            binding.tvStatus.setBackgroundResource(bg)
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, color))

            binding.root.setOnClickListener {
                onItemClick(order)
            }
        }
    }
}
