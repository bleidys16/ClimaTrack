package com.example.climatrack.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityEquipmentFormBinding
import com.example.climatrack.models.Equipo
import com.example.climatrack.repositories.EquipoRepository

class EquipmentFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquipmentFormBinding
    private lateinit var equipoRepository: EquipoRepository
    private var equipmentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        equipoRepository = EquipoRepository(this)
        equipmentId = intent.getIntExtra("EQUIPMENT_ID", -1)

        setupSpinner()
        
        if (equipmentId != -1) {
            loadEquipmentData()
            binding.tvFormTitle.text = "Editar Equipo"
            binding.btnDelete.visibility = View.VISIBLE
        }

        binding.btnSave.setOnClickListener { saveEquipment() }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.equipment_status_array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnStatus.adapter = adapter
    }

    private fun loadEquipmentData() {
        val equip = equipoRepository.getById(equipmentId)
        equip?.let {
            binding.etCode.setText(it.codigo)
            binding.etType.setText(it.tipo)
            binding.etBrand.setText(it.marca)
            binding.etModel.setText(it.modelo)
            binding.etSerial.setText(it.serial)
            binding.etCapacity.setText(it.capacidad)
            binding.etLocation.setText(it.ubicacion)
            
            val statusArray = resources.getStringArray(R.array.equipment_status_array)
            val pos = statusArray.indexOf(it.estado)
            if (pos >= 0) binding.spnStatus.setSelection(pos)
        }
    }

    private fun saveEquipment() {
        val code = binding.etCode.text.toString().trim()
        val type = binding.etType.text.toString().trim()
        val brand = binding.etBrand.text.toString().trim()
        val model = binding.etModel.text.toString().trim()
        val serial = binding.etSerial.text.toString().trim()
        val capacity = binding.etCapacity.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val status = binding.spnStatus.selectedItem.toString()

        if (code.isEmpty() || type.isEmpty() || brand.isEmpty() || model.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            return
        }

        val equipo = Equipo(
            id = if (equipmentId == -1) 0 else equipmentId,
            codigo = code,
            tipo = type,
            marca = brand,
            modelo = model,
            serial = serial,
            capacidad = capacity,
            ubicacion = location,
            clienteId = 1, // Por simplicidad asignamos al cliente 1
            estado = status
        )

        val result = if (equipmentId == -1) {
            equipoRepository.create(equipo)
        } else {
            equipoRepository.update(equipo).toLong()
        }

        if (result > 0) {
            Toast.makeText(this, "Equipo guardado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al guardar equipo (Verifique el código)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Equipo")
            .setMessage("¿Está seguro de eliminar este equipo?")
            .setPositiveButton("Eliminar") { _, _ ->
                equipoRepository.delete(equipmentId)
                Toast.makeText(this, "Equipo eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
