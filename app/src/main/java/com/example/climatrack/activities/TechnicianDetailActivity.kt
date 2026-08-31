package com.example.climatrack.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.TechnicianHistoryAdapter
import com.example.climatrack.databinding.ActivityTechnicianDetailBinding
import com.example.climatrack.repositories.UsuarioRepository

class TechnicianDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityTechnicianDetailBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private var techId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTechnicianDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        usuarioRepository = UsuarioRepository(this)
        techId = intent.getIntExtra("TECH_ID", -1)

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (techId != -1) {
            loadTechnicianInfo()
        } else {
            finish()
        }
    }

    private fun loadTechnicianInfo() {
        val tech = usuarioRepository.getById(techId)
        val stats = usuarioRepository.getTechnicianStats().find { it.id == techId }
        val history = usuarioRepository.getTechnicianHistory(techId)

        tech?.let {
            binding.tvDetailName.text = it.nombre
            binding.tvDetailEmail.text = it.email ?: "Sin email"
            binding.tvDetailPhone.text = it.telefono ?: "Sin teléfono"
        }

        stats?.let {
            binding.tvDetailRole.text = "Técnico - ${it.trabajosRealizados} servicios"
            binding.detailRatingBar.rating = it.promedioCalificacion.toFloat()
            binding.tvRatingValue.text = String.format(java.util.Locale.getDefault(), "%.1f", it.promedioCalificacion)
        }

        binding.rvActivityHistory.layoutManager = LinearLayoutManager(this)
        binding.rvActivityHistory.adapter = TechnicianHistoryAdapter(history)
    }
}
