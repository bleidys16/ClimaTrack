package com.example.climatrack.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.climatrack.databinding.ActivitySparePartsBinding
import com.example.climatrack.models.DetalleRepuesto
import com.example.climatrack.models.Repuesto
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.RepuestoRepository
import com.example.climatrack.repositories.ServicioRepository

class SparePartsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySparePartsBinding
    private lateinit var repuestoRepository: RepuestoRepository
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private var orderId: Int = -1
    private var sparePartsList: List<Repuesto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySparePartsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repuestoRepository = RepuestoRepository(this)
        servicioRepository = ServicioRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        loadSpareParts()
        binding.btnAddSparePart.setOnClickListener { saveSparePart() }
    }

    private fun loadSpareParts() {
        sparePartsList = repuestoRepository.getAll()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sparePartsList.map { it.nombre })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnSpareParts.adapter = adapter
    }

    private fun saveSparePart() {
        val qtyStr = binding.etQuantity.text.toString()
        val obs = binding.etObservation.text.toString()
        
        if (qtyStr.isEmpty()) {
            Toast.makeText(this, "Ingrese la cantidad", Toast.LENGTH_SHORT).show()
            return
        }
        
        val qty = qtyStr.toInt()
        if (qty <= 0) {
            Toast.makeText(this, "La cantidad debe ser mayor a cero", Toast.LENGTH_SHORT).show()
            return
        }

        val maintenance = mantenimientoRepository.getByOrdenId(orderId)
        if (maintenance == null) {
            Toast.makeText(this, "Primero debe registrar el mantenimiento", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRepuesto = sparePartsList[binding.spnSpareParts.selectedItemPosition]
        
        val detalle = DetalleRepuesto(
            mantenimientoId = maintenance.id,
            repuestoId = selectedRepuesto.id,
            cantidad = qty,
            observacion = obs
        )

        val result = servicioRepository.addRepuesto(detalle)
        if (result > 0) {
            Toast.makeText(this, "Repuesto agregado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al agregar repuesto", Toast.LENGTH_SHORT).show()
        }
    }
}
