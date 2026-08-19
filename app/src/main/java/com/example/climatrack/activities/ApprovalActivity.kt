package com.example.climatrack.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.climatrack.databinding.ActivityApprovalBinding
import com.example.climatrack.models.Aprobacion
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.ServicioRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApprovalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApprovalBinding
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var ordenRepository: OrdenRepository
    private var orderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        servicioRepository = ServicioRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        ordenRepository = OrdenRepository(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        loadSummary()
        binding.btnSaveApproval.setOnClickListener { saveApproval() }
    }

    private fun loadSummary() {
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        
        if (mant != null && info != null) {
            val summary = "Orden: ${info.numero}\n" +
                    "Equipo: ${info.equipoNombre}\n" +
                    "Trabajo: ${mant.trabajoRealizado}\n" +
                    "Diagnóstico: ${mant.diagnostico}"
            binding.tvSummary.text = summary
        }
    }

    private fun saveApproval() {
        val clientName = binding.etClientName.text.toString().trim()
        val accepted = if (binding.swAccept.isChecked) 1 else 0

        if (clientName.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del cliente", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val aprobacion = Aprobacion(
            ordenId = orderId,
            cliente = clientName,
            aceptado = accepted,
            fecha = date
        )

        val result = servicioRepository.addAprobacion(aprobacion)
        if (result > 0) {
            Toast.makeText(this, "Aprobación registrada correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al registrar aprobación", Toast.LENGTH_SHORT).show()
        }
    }
}
