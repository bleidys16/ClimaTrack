package com.example.climatrack.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.StatAdapter
import com.example.climatrack.databinding.ActivityFailureIntelligenceBinding
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.ServicioRepository

class FailureIntelligenceActivity : BaseActivity() {

    private lateinit var binding: ActivityFailureIntelligenceBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var servicioRepository: ServicioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFailureIntelligenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        ordenRepository = OrdenRepository(this)
        servicioRepository = ServicioRepository(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        loadStats()
    }

    private fun loadStats() {
        // Parts Stats
        val topParts = servicioRepository.getTopPartsStats()
        binding.rvTopParts.layoutManager = LinearLayoutManager(this)
        binding.rvTopParts.adapter = StatAdapter(topParts)

        // Brands Stats
        val topBrands = ordenRepository.getTopBrandsStats()
        binding.rvTopBrands.layoutManager = LinearLayoutManager(this)
        binding.rvTopBrands.adapter = StatAdapter(topBrands)
    }
}
