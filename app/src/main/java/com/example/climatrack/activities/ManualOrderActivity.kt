package com.example.climatrack.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.climatrack.databinding.ActivityManualOrderBinding
import com.example.climatrack.models.Orden
import com.example.climatrack.repositories.EquipoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.UsuarioRepository
import java.text.SimpleDateFormat
import java.util.*

class ManualOrderActivity : BaseActivity() {

    private lateinit var binding: ActivityManualOrderBinding
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var equipoRepository: EquipoRepository

    private var selectedClientId: Int = -1
    private var selectedEquipmentId: Int = -1
    private var selectedTechId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Obtenemos el AppBarLayout si existe en el layout
        val appBar = binding.root.findViewById<com.google.android.material.appbar.AppBarLayout>(com.example.climatrack.R.id.appBarLayout) ?: 
                     binding.root.findViewWithTag<View>("app_bar")
        
        setupEdgeToEdge(binding.root, appBar ?: binding.toolbar)

        ordenRepository = OrdenRepository(this)
        usuarioRepository = UsuarioRepository(this)
        equipoRepository = EquipoRepository(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupSpinners()
        binding.btnCreateOrder.setOnClickListener { createOrder() }
    }

    private fun setupSpinners() {
        // Clientes
        val clientes = usuarioRepository.getAllClientes()
        val clientNames = clientes.map { it.nombre }
        val clientAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, clientNames)
        binding.spinnerClient.setAdapter(clientAdapter)
        binding.spinnerClient.setOnItemClickListener { _, _, position, _ ->
            selectedClientId = clientes[position].id
        }

        // Equipos
        val equipos = equipoRepository.getAll()
        val equipmentNames = equipos.map { "${it.marca} ${it.modelo} (${it.codigo})" }
        val equipAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, equipmentNames)
        binding.spinnerEquipment.setAdapter(equipAdapter)
        binding.spinnerEquipment.setOnItemClickListener { _, _, position, _ ->
            selectedEquipmentId = equipos[position].id
        }

        // Técnicos
        val tecnicos = usuarioRepository.getAllTecnicos()
        val techNames = tecnicos.map { it.nombre }
        val techAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, techNames)
        binding.spinnerTech.setAdapter(techAdapter)
        binding.spinnerTech.setOnItemClickListener { _, _, position, _ ->
            selectedTechId = tecnicos[position].id
        }

        // Tipo de Servicio
        val types = listOf("PREVENTIVO", "CORRECTIVO", "INSPECCION")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        binding.spinnerServiceType.setAdapter(typeAdapter)
    }

    private fun createOrder() {
        val type = binding.spinnerServiceType.text.toString()
        val desc = binding.etDescription.text.toString().trim()

        if (selectedClientId == -1 || selectedEquipmentId == -1 || type.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione cliente, equipo y tipo", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
        val date = sdf.format(Date())
        val orderNum = "ADM-" + System.currentTimeMillis().toString().takeLast(6)

        val newOrder = Orden(
            numero = orderNum,
            fecha = date,
            clienteId = selectedClientId,
            equipoId = selectedEquipmentId,
            tecnicoId = selectedTechId,
            tipoServicio = type,
            descripcion = desc,
            estado = if (selectedTechId != null) "PENDIENTE" else "SIN ASIGNAR"
        )

        val result = ordenRepository.create(newOrder)
        if (result > 0) {
            Toast.makeText(this, "Orden creada exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al crear la orden", Toast.LENGTH_SHORT).show()
        }
    }
}
