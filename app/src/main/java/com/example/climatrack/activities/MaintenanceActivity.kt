package com.example.climatrack.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Toast
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

        mantenimientoRepository = MantenimientoRepository(this)
        ordenRepository = OrdenRepository(this)
        sessionManager = SessionManager(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupEdgeToEdge(binding.root, binding.appBarLayout)
        setupPickers()
        loadOrderInfo()
        loadExistingData()

        binding.btnSaveMaint.setOnClickListener { saveMaintenance() }
        binding.tvToolbarSave.setOnClickListener { saveMaintenance() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadOrderInfo() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvOrderNumDisplay.text = "Orden: ${it.numero}"
            binding.tvClientDisplay.text = "Cliente: ${it.clienteNombre}"
            binding.tvEquipDisplay.text = "Equipo: ${it.equipoNombre}"
            binding.tvStatusDisplay.text = it.estado
        }
    }

    private fun setupPickers() {
        val calendar = Calendar.getInstance()

        binding.etDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                binding.etDate.setText(date)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etTimeStart.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                binding.etTimeStart.setText(time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun loadExistingData() {
        val mant = mantenimientoRepository.getByOrdenId(orderId)
        mant?.let {
            binding.etDiagnosis.setText(it.diagnostico)
            binding.etWorkDone.setText(it.trabajoRealizado)
            binding.etDate.setText(it.fecha.split(" ").getOrNull(0) ?: it.fecha)
            binding.etTimeStart.setText(it.fecha.split(" ").getOrNull(1) ?: "")
        } ?: run {
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            binding.etDate.setText(currentDate)
            binding.etTimeStart.setText(currentTime)
        }
        
        ordenRepository.getById(orderId)?.let {
            if (it.precioMantenimiento > 0) {
                binding.etPrice.setText(it.precioMantenimiento.toString())
            }
        }
    }

    private fun saveMaintenance() {
        val diag = binding.etDiagnosis.text.toString().trim()
        val work = binding.etWorkDone.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val time = binding.etTimeStart.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()

        if (diag.isEmpty() || work.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        if (price <= 0) {
            Toast.makeText(this, "Ingrese un costo de mantenimiento válido", Toast.LENGTH_SHORT).show()
            return
        }

        val maintenance = Mantenimiento(
            ordenId = orderId,
            fecha = "$date $time",
            diagnostico = diag,
            trabajoRealizado = work,
            estadoEquipo = "OPERATIVO",
            tiempoEmpleado = "N/A"
        )

        val existing = mantenimientoRepository.getByOrdenId(orderId)
        val result = if (existing != null) {
            mantenimientoRepository.update(maintenance)
        } else {
            mantenimientoRepository.create(maintenance).toInt()
        }

        if (result > 0) {
            updateOrderPrice(price)
        } else {
            Toast.makeText(this, "Error al guardar mantenimiento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOrderPrice(price: Double) {
        val db = com.example.climatrack.database.DatabaseHelper(this).writableDatabase
        val values = android.content.ContentValues().apply {
            put(com.example.climatrack.database.DatabaseHelper.COL_ORDEN_PRECIO_MANT, price)
        }
        val rows = db.update(com.example.climatrack.database.DatabaseHelper.TABLE_ORDENES, values,
            "${com.example.climatrack.database.DatabaseHelper.COL_ORDEN_ID}=?", arrayOf(orderId.toString()))
        
        if (rows > 0) {
            Toast.makeText(this, "Mantenimiento guardado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al actualizar costo del servicio", Toast.LENGTH_SHORT).show()
        }
    }
}
