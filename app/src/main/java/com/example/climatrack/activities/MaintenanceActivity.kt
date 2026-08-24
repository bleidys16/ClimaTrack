package com.example.climatrack.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityMaintenanceBinding
import com.example.climatrack.models.Mantenimiento
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MaintenanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaintenanceBinding
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var ordenRepository: OrdenRepository
    private var orderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mantenimientoRepository = MantenimientoRepository(this)
        ordenRepository = OrdenRepository(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupSpinner()
        loadOrderInfo()
        loadExistingData()

        binding.btnSaveMaint.setOnClickListener { saveMaintenance() }
    }

    private fun loadOrderInfo() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvOrderDetail.text = "OT: ${it.numero} - ${it.clienteNombre}"
            binding.etServiceType.setText(it.tipoServicio)
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.equipment_status_array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnEquipStatus.adapter = adapter
    }

    private fun loadExistingData() {
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        mant?.let {
            binding.etDiagnosis.setText(it.diagnostico)
            binding.etWorkDone.setText(it.trabajoRealizado)
            binding.etObservations.setText(it.observaciones)
            binding.etRecommendations.setText(it.recomendaciones)
            binding.etTimeSpent.setText(it.tiempoEmpleado)
            
            val statusArray = resources.getStringArray(R.array.equipment_status_array)
            val pos = statusArray.indexOf(it.estadoEquipo)
            if (pos >= 0) binding.spnEquipStatus.setSelection(pos)
        }
    }

    private fun saveMaintenance() {
        val diag = binding.etDiagnosis.text.toString().trim()
        val work = binding.etWorkDone.text.toString().trim()
        val obs = binding.etObservations.text.toString().trim()
        val recom = binding.etRecommendations.text.toString().trim()
        val time = binding.etTimeSpent.text.toString().trim()
        val status = binding.spnEquipStatus.selectedItem.toString()

        if (diag.isEmpty() || work.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val maintenance = Mantenimiento(
            ordenId = orderId,
            fecha = date,
            diagnostico = diag,
            trabajoRealizado = work,
            observaciones = obs,
            recomendaciones = recom,
            estadoEquipo = status,
            tiempoEmpleado = time
        )

        val result = mantenimientoRepository.create(maintenance)

        if (result > 0) {
            // Actualizar estado de la orden a EN PROCESO
            ordenRepository.updateEstado(orderId, "EN PROCESO")
            Toast.makeText(this, "Mantenimiento registrado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al guardar el mantenimiento", Toast.LENGTH_SHORT).show()
        }
    }
}
