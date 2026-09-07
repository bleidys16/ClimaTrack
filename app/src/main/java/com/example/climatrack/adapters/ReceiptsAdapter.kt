package com.example.climatrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.databinding.ItemReceiptBinding
import com.example.climatrack.models.OrdenInfo
import java.util.Locale

class ReceiptsAdapter(
    private var orders: List<OrdenInfo>,
    private val onDownloadClick: (OrdenInfo) -> Unit
) : RecyclerView.Adapter<ReceiptsAdapter.ReceiptViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val binding = ItemReceiptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceiptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateList(newList: List<OrdenInfo>) {
        orders = newList
        notifyDataSetChanged()
    }

    inner class ReceiptViewHolder(private val binding: ItemReceiptBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrdenInfo) {
            binding.tvOrderNum.text = "Orden: ${order.numero}"
            binding.tvDate.text = "Finalizada: ${order.fecha}"
            
            val total = order.precioServicio + (order.precioMantenimiento ?: 0.0)
            binding.tvTotal.text = "Total Pagado: $${String.format(Locale.getDefault(), "%.2f", total)}"

            binding.btnDownload.setOnClickListener {
                onDownloadClick(order)
            }
        }
    }
}
