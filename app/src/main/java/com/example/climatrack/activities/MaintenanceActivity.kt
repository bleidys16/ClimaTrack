package com.example.climatrack.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityMaintenanceBinding
import com.example.climatrack.models.Mantenimiento
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class MaintenanceActivity : BaseActivity() {

    private lateinit var binding: ActivityMaintenanceBinding
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var ordenRepository: OrdenRepository
    private lateinit var sessionManager: SessionManager
    private var orderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        mantenimientoRepository = MantenimientoRepository(this)
        ordenRepository = OrdenRepository(this)
        sessionManager = SessionManager(this)
        
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupDropdowns()
        setupPickers()
        loadOrderInfo()
        loadExistingData()

        binding.btnSaveMaint.setOnClickListener { saveMaintenance() }
        binding.tvToolbarSave.setOnClickListener { saveMaintenance() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadOrderInfo() {
        // Obtenemos la info de la orden sin filtrar por técnico para asegurar que cargue
        // O mejor, usamos el ID del técnico actual
        val tecnicoId = sessionManager.getUserId()
        val info = ordenRepository.getAllInfoByTecnico(tecnicoId).find { it.id == orderId }
        info?.let {
            binding.tvOrderNumDisplay.text = "Orden: ${it.numero}"
            binding.tvClientDisplay.text = "Cliente: ${it.clienteNombre}"
            binding.tvEquipDisplay.text = "Equipo: ${it.equipoNombre}"
            
            when (it.tipoServicio.uppercase()) {
                "PREVENTIVO" -> binding.toggleServiceType.check(R.id.btnPrev)
                "CORRECTIVO" -> binding.toggleServiceType.check(R.id.btnCorr)
                "ASESORÍA" -> binding.toggleServiceType.check(R.id.btnAsesoria)
                "INSPECCIÓN" -> binding.toggleServiceType.check(R.id.btnInsp)
            }
        }
    }

    private fun setupDropdowns() {
        val statusList = resources.getStringArray(R.array.equipment_status_array)
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusList)
        (binding.spnEquipStatus as? AutoCompleteTextView)?.setAdapter(statusAdapter)

        val timeList = arrayOf("30m", "1h 00m", "1h 30m", "2h 00m", "2h 30m", "3h 00m", "4h+")
        val timeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, timeList)
        (binding.spnTimeSpent as? AutoCompleteTextView)?.setAdapter(timeAdapter)

        val technicianList = arrayOf(sessionManager.getUserName() ?: "Técnico 01", "Supervisor")
        val techAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, technicianList)
        (binding.spnTecnico as? AutoCompleteTextView)?.setAdapter(techAdapter)
        (binding.spnTecnico as? AutoCompleteTextView)?.setText(technicianList[0], false)
    }

    private fun setupPickers() {
        val calendar = Calendar.getInstance()
        
        binding.etDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                val date = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year)
                binding.etDate.setText(date)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etTimeStart.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                binding.etTimeStart.setText(time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun loadExistingData() {
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        mant?.let {
            binding.etDiagnosis.setText(it.diagnostico)
            binding.etWorkDone.setText(it.trabajoRealizado)
            binding.etObservations.setText(it.observaciones)
            binding.etDate.setText(it.fecha)
            (binding.spnEquipStatus as? AutoCompleteTextView)?.setText(it.estadoEquipo, false)
            (binding.spnTimeSpent as? AutoCompleteTextView)?.setText(it.tiempoEmpleado, false)
        } ?: run {
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            binding.etDate.setText(currentDate)
        }
    }

    private fun saveMaintenance() {
        val diag = binding.etDiagnosis.text.toString().trim()
        val work = binding.etWorkDone.text.toString().trim()
        val obs = binding.etObservations.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val status = binding.spnEquipStatus.text.toString()
        val time = binding.spnTimeSpent.text.toString()
        val priceStr = binding.etPrice.text.toString().trim()

        if (diag.isEmpty() || work.isEmpty() || date.isEmpty() || status.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            return
        }

        val maintenance = Mantenimiento(
            ordenId = orderId,
            fecha = date,
            diagnostico = diag,
            trabajoRealizado = work,
            observaciones = obs,
            recomendaciones = "",
            estadoEquipo = status,
            tiempoEmpleado = time
        )

        // Verificamos si ya existe para actualizar o crear
        val existing = mantenimientoRepository.getByOrdenId(orderId)
        val result = if (existing != null) {
            mantenimientoRepository.update(maintenance).toLong()
        } else {
            mantenimientoRepository.create(maintenance)
        }

        if (result > 0) {
            val price = priceStr.toDoubleOrNull() ?: 0.0
            if (price > 0) {
                ordenRepository.updatePrecio(orderId, price)
                Toast.makeText(this, "Mantenimiento guardado y cotización enviada", Toast.LENGTH_SHORT).show()
            } else {
                ordenRepository.updateEstado(orderId, "EN PROCESO")
                Toast.makeText(this, "Mantenimiento registrado correctamente", Toast.LENGTH_SHORT).show()
            }
            finish()
        } else {
            Toast.makeText(this, "Error al guardar el mantenimiento", Toast.LENGTH_SHORT).show()
        }
    }
}
