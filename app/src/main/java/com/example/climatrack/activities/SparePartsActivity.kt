package com.example.climatrack.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.climatrack.R
import com.example.climatrack.adapters.SparePartsAdapter
import com.example.climatrack.databinding.ActivitySparePartsBinding
import com.example.climatrack.databinding.DialogAddSparePartBinding
import com.example.climatrack.models.DetalleRepuesto
import com.example.climatrack.models.Repuesto
import com.example.climatrack.repositories.MantenimientoRepository
import com.example.climatrack.repositories.OrdenRepository
import com.example.climatrack.repositories.RepuestoRepository
import com.example.climatrack.repositories.ServicioRepository
import java.text.NumberFormat
import java.util.*

class SparePartsActivity : BaseActivity() {

    private lateinit var binding: ActivitySparePartsBinding
    private lateinit var repuestoRepository: RepuestoRepository
    private lateinit var servicioRepository: ServicioRepository
    private lateinit var mantenimientoRepository: MantenimientoRepository
    private lateinit var adapter: SparePartsAdapter
    
    private var orderId: Int = -1
    private var mantenimientoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySparePartsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        repuestoRepository = RepuestoRepository(this)
        servicioRepository = ServicioRepository(this)
        mantenimientoRepository = MantenimientoRepository(this)
        
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadOrderInfo()
        
        binding.ivAddSparePart.setOnClickListener { showAddDialog() }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = SparePartsAdapter(emptyList()) { item ->
            confirmDelete(item.id)
        }
        binding.rvSpareParts.layoutManager = LinearLayoutManager(this)
        binding.rvSpareParts.adapter = adapter
    }

    private fun loadOrderInfo() {
        val ordenRepo = OrdenRepository(this)
        val info = ordenRepo.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvOrderNumDisplay.text = "Orden: ${it.numero}"
            binding.tvClientDisplay.text = "Cliente: ${it.clienteNombre}"
            binding.tvEquipDisplay.text = "Equipo: ${it.equipoNombre}"
            binding.tvStatusDisplay.text = it.estado
        }

        val mant = mantenimientoRepository.getByOrdenId(orderId)
        if (mant != null) {
            mantenimientoId = mant.id
            loadPartsList()
        } else {
            Toast.makeText(this, "Debe registrar mantenimiento primero", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadPartsList() {
        if (mantenimientoId == -1) return
        val list = servicioRepository.getRepuestosByMantenimiento(mantenimientoId)
        adapter.updateList(list)
        
        // Calculate Total
        val total = list.sumOf { it.precio * it.cantidad }
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        binding.tvTotalValue.text = formatter.format(total)
    }

    private fun showAddDialog() {
        if (mantenimientoId == -1) {
            Toast.makeText(this, "Registre el mantenimiento antes de agregar repuestos", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogAddSparePartBinding.inflate(LayoutInflater.from(this))
        val spareParts = repuestoRepository.getAll()
        val adapterParts = ArrayAdapter(this, android.R.layout.simple_list_item_1, spareParts.map { it.nombre })
        
        (dialogBinding.tilSelectPart.editText as? AutoCompleteTextView)?.setAdapter(adapterParts)
        
        var selectedPart: Repuesto? = null
        (dialogBinding.tilSelectPart.editText as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
            selectedPart = spareParts[position]
        }

        AlertDialog.Builder(this)
            .setTitle("Agregar Repuesto")
            .setView(dialogBinding.root)
            .setPositiveButton("Agregar") { _, _ ->
                val qty = dialogBinding.etQuantity.text.toString().toIntOrNull() ?: 0
                if (selectedPart != null && qty > 0) {
                    val detail = DetalleRepuesto(
                        mantenimientoId = mantenimientoId,
                        repuestoId = selectedPart!!.id,
                        cantidad = qty,
                        observacion = dialogBinding.etObs.text.toString(),
                        precioHistorico = selectedPart!!.precio
                    )
                    servicioRepository.addRepuesto(detail)
                    loadPartsList()
                } else {
                    Toast.makeText(this, "Seleccione un repuesto y cantidad válida", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(detalleId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Registro")
            .setMessage("¿Desea eliminar este repuesto de la lista?")
            .setPositiveButton("Eliminar") { _, _ ->
                servicioRepository.deleteRepuesto(detalleId)
                loadPartsList()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
