package com.example.climatrack.activities

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.TechnicianHistoryAdapter
import com.example.climatrack.databinding.ActivityTechnicianDetailBinding
import com.example.climatrack.models.ActividadTecnico
import com.example.climatrack.repositories.UsuarioRepository
import java.text.SimpleDateFormat
import java.util.*

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
            binding.tvRatingValue.text = String.format(Locale.getDefault(), "%.1f", it.promedioCalificacion)
        }

        calculateProductivity(history)

        binding.rvActivityHistory.layoutManager = LinearLayoutManager(this)
        binding.rvActivityHistory.adapter = TechnicianHistoryAdapter(history)
    }

    private fun calculateProductivity(history: List<ActividadTecnico>) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateSdf.format(Date())
        
        var totalMinutesWeek = 0L
        var todayMinutes = 0L

        history.forEach { item ->
            if (!item.horaInicio.isNullOrEmpty() && !item.horaFin.isNullOrEmpty()) {
                try {
                    val start = sdf.parse(item.horaInicio)
                    val end = sdf.parse(item.horaFin)
                    if (start != null && end != null) {
                        val diff = end.time - start.time
                        val minutes = diff / (1000 * 60)
                        if (minutes > 0) {
                            totalMinutesWeek += minutes
                            if (item.fecha == today) {
                                todayMinutes = minutes
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        binding.tvTodayHours.text = formatMinutes(todayMinutes)
        binding.tvWeekHours.text = formatMinutes(totalMinutesWeek)
    }

    private fun formatMinutes(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return "${h}h ${m}m"
    }
}
