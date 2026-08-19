package com.example.climatrack.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.HistoryAdapter
import com.example.climatrack.databinding.ActivityHistoryBinding
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.utils.SessionManager

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mantenimientoRepository = MantenimientoRepository(this)
        sessionManager = SessionManager(this)

        setupToolbar()
        loadHistory()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadHistory() {
        val tecnicoId = sessionManager.getUserId()
        val history = mantenimientoRepository.getHistorial(tecnicoId)
        
        val adapter = HistoryAdapter(history)
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter
    }
}
