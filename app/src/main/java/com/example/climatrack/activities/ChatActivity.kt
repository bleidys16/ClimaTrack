package com.example.climatrack.activities

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.databinding.ActivityChatBinding
import com.example.climatrack.models.Mensaje
import com.example.climatrack.repositories.ChatRepository
import com.example.climatrack.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatRepository: ChatRepository
    private lateinit var sessionManager: SessionManager
    private var orderId: Int = -1
    private lateinit var adapter: com.example.climatrack.adapters.ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        chatRepository = ChatRepository(this)
        sessionManager = SessionManager(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        listenToMessages()

        binding.btnSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        val orderNum = intent.getStringExtra("ORDER_NUM")
        binding.toolbar.title = "Chat Orden: $orderNum"
    }

    private fun setupRecyclerView() {
        adapter = com.example.climatrack.adapters.ChatAdapter(sessionManager.getUserId())
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter
    }

    private fun listenToMessages() {
        chatRepository.listenToMessages(orderId) { messages ->
            adapter.updateList(messages)
            binding.rvChat.scrollToPosition(messages.size - 1)
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = sdf.format(Date())

        val msg = Mensaje(
            ordenId = orderId,
            remitenteId = sessionManager.getUserId(),
            nombreRemitente = sessionManager.getUserName() ?: "Usuario",
            texto = text,
            fecha = now
        )

        chatRepository.sendMessage(msg)
        binding.etMessage.text.clear()
    }
}
