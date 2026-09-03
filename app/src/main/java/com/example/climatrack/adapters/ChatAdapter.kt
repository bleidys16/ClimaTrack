package com.example.climatrack.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.climatrack.R
import com.example.climatrack.databinding.ItemMessageBinding
import com.example.climatrack.models.Mensaje

class ChatAdapter(private val currentUserId: Int) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private var messages: List<Mensaje> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun updateList(newList: List<Mensaje>) {
        messages = newList
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Mensaje) {
            val isMe = msg.remitenteId == currentUserId
            
            val params = binding.cardMessage.layoutParams as LinearLayout.LayoutParams
            if (isMe) {
                params.gravity = Gravity.END
                binding.cardMessage.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.american_blue))
                binding.tvSender.text = "Tú"
            } else {
                params.gravity = Gravity.START
                binding.cardMessage.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.chinese_black))
                binding.tvSender.text = msg.nombreRemitente
            }
            binding.cardMessage.layoutParams = params
            
            binding.tvText.text = msg.texto
            binding.tvTime.text = msg.fecha.takeLast(8) // Just time
        }
    }
}
