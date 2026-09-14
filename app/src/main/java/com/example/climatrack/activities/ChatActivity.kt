package com.example.climatrack.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.ChatAdapter
import com.example.climatrack.databinding.ActivityChatBinding
import com.example.climatrack.repositories.ChatRepository
import com.example.climatrack.utils.SessionManager

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatRepository: ChatRepository
    private lateinit var sessionManager: SessionManager
    private var orderId: String = ""
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatRepository = ChatRepository(this)
        sessionManager = SessionManager(this)
        orderId = intent.getStringExtra("ORDER_ID") ?: ""
        val orderNum = intent.getStringExtra("ORDER_NUM")

        setupToolbar()
        binding.toolbar.title = "Chat Orden: $orderNum"
        setupRecyclerView()
        loadLocalMessages()
        listenToMessages()

        binding.btnSendMessage.setOnClickListener { sendMessage() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(sessionManager.getUserId())
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter
    }

    private fun loadLocalMessages() {
        val localMessages = chatRepository.getMessagesLocal(orderId)
        adapter.updateList(localMessages)
    }

    private fun listenToMessages() {
        chatRepository.listenToMessages(orderId) { messages ->
            adapter.updateList(messages)
            if (messages.isNotEmpty()) {
                binding.rvChat.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val msg = com.example.climatrack.models.Mensaje(
            ordenId = orderId,
            remitenteId = sessionManager.getUserId(),
            nombreRemitente = "Usuario", // Simplifying
            texto = text,
            fecha = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        )

        chatRepository.sendMessage(msg)
        binding.etMessage.setText("")
        loadLocalMessages()
    }
}
