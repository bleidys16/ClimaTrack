package com.example.climatrack.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.adapters.SparePartsAdapter
import com.example.climatrack.databinding.ActivitySparePartsBinding
import com.example.climatrack.databinding.DialogAddSparePartBinding
import com.example.climatrack.models.DetalleRepuesto
import com.example.climatrack.models.Repuesto
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.RepuestoRepository
import com.example.climatrack.repositories.ServicioRepository
import java.util.*

class SparePartsActivity : BaseActivity() {

    private lateinit var binding: ActivitySparePartsBinding
    private lateinit var repuestoRepository: RepuestoRepository
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var adapter: SparePartsAdapter

    private var orderId: String = ""
    private var mantenimientoId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySparePartsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repuestoRepository = RepuestoRepository(this)
        servicioRepository = ServicioRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        
        orderId = intent.getStringExtra("ORDER_ID") ?: ""
        mantenimientoId = mantenimientoRepository.getByOrdenId(orderId)?.id ?: ""

        if (mantenimientoId.isEmpty()) {
            Toast.makeText(this, "Debe registrar primero el mantenimiento", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadOrderInfo()
        loadPartsList()

        binding.ivAddSparePart.setOnClickListener { showAddDialog() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = SparePartsAdapter(emptyList()) { partInfo ->
            confirmDelete(partInfo.id)
        }
        binding.rvSpareParts.layoutManager = LinearLayoutManager(this)
        binding.rvSpareParts.adapter = adapter
    }

    private fun loadOrderInfo() {
        val ordenRepository = com.example.climatrack.repositories.OrdenRepository(this)
        val info = ordenRepository.getAllInfoByTecnico("-1").find { it.id == orderId }
        info?.let {
            binding.tvOrderNumDisplay.text = "Orden: ${it.numero}"
            binding.tvEquipDisplay.text = "Equipo: ${it.equipoNombre}"
            binding.tvClientDisplay.text = "Cliente: ${it.clienteNombre}"
            binding.tvStatusDisplay.text = it.estado
        }
    }

    private fun loadPartsList() {
        val parts = servicioRepository.getRepuestosByMantenimiento(mantenimientoId)
        adapter.updateList(parts)
        
        val total = parts.sumOf { it.precio * it.cantidad }
        binding.tvTotalValue.text = "$${String.format(Locale.getDefault(), "%.2f", total)}"
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddSparePartBinding.inflate(layoutInflater)
        val parts = repuestoRepository.getAll()
        val partNames = parts.map { it.nombre }
        
        val adapterDropdown = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, partNames)
        val autoComplete = dialogBinding.tilSelectPart.editText as AutoCompleteTextView
        autoComplete.setAdapter(adapterDropdown)
        autoComplete.setTextColor(resources.getColor(com.example.climatrack.R.color.white, null))

        var selectedPart: Repuesto? = null
        autoComplete.setOnItemClickListener { _, _, position, _ ->
            selectedPart = parts[position]
            dialogBinding.etUnitPrice.setText(selectedPart?.precio?.toString() ?: "")
        }

        AlertDialog.Builder(this)
            .setTitle("Agregar Repuesto")
            .setView(dialogBinding.root)
            .setPositiveButton("Agregar") { _, _ ->
                val qty = dialogBinding.etQuantity.text.toString().toIntOrNull() ?: 0
                val price = dialogBinding.etUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
                val part = selectedPart
                
                if (part != null && qty > 0 && price > 0) {
                    val detail = DetalleRepuesto(
                        mantenimientoId = mantenimientoId,
                        repuestoId = part.id,
                        cantidad = qty,
                        precioUnitario = price,
                        precioHistorico = price * qty,
                        observacion = dialogBinding.etObs.text.toString()
                    )
                    servicioRepository.addRepuesto(detail)
                    loadPartsList()
                } else {
                    Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(partId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar repuesto")
            .setMessage("¿Desea quitar este repuesto de la lista?")
            .setPositiveButton("Eliminar") { _, _ ->
                servicioRepository.deleteRepuesto(partId)
                loadPartsList()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
