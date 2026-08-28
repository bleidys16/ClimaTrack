package com.example.climatrack.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivityOrderDetailBinding
import com.example.climatrack.repositories.OrdenRepository

class OrderDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var ordenRepository: OrdenRepository
    private var orderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        ordenRepository = OrdenRepository(this)
        orderId = intent.getIntExtra("ORDER_ID", -1)

        if (orderId == -1) {
            finish()
            return
        }

        loadOrderData()
        setupToolbar()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadOrderData() {
        val info = ordenRepository.getAllInfoByTecnico(-1).find { it.id == orderId }
        info?.let {
            binding.tvOrderNum.text = "Orden: ${it.numero}"
            binding.tvStatus.text = it.estado
            binding.tvClientInfo.text = "Cliente: ${it.clienteNombre}"
            binding.tvEquipInfo.text = "Equipo: ${it.equipoNombre}"
            binding.tvServiceType.text = "Servicio: ${it.tipoServicio}"

            // Status color
            val (containerColor, textColor) = when (it.estado) {
                "PENDIENTE" -> R.color.status_pending_container to R.color.status_pending
                "EN PROCESO" -> R.color.status_in_progress_container to R.color.status_in_progress
                "FINALIZADA" -> R.color.status_finished_container to R.color.status_finished
                "CANCELADA" -> R.color.status_error_container to R.color.status_error
                else -> R.color.status_pending_container to R.color.status_pending
            }
            binding.tvStatus.backgroundTintList = ContextCompat.getColorStateList(this, containerColor)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, textColor))
            
            if (it.estado == "FINALIZADA") {
                binding.btnFinishOrder.isEnabled = false
                binding.tvFinishLabel.text = "ORDEN FINALIZADA"
                binding.llFinishContainer.alpha = 0.5f
            }
        }
    }

    private fun setupButtons() {
        binding.btnRegisterMaint.setOnClickListener {
            Intent(this, MaintenanceActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }
        
        binding.btnSpareParts.setOnClickListener {
            Intent(this, SparePartsActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }
        
        binding.btnEvidence.setOnClickListener {
            Intent(this, EvidenceActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }
        
        binding.btnLocation.setOnClickListener {
            Intent(this, LocationActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }
        
        binding.btnApproval.setOnClickListener {
            Intent(this, ApprovalActivity::class.java).also {
                it.putExtra("ORDER_ID", orderId)
                startActivity(it)
            }
        }

        binding.btnFinishOrder.setOnClickListener { confirmFinish() }
    }

    private fun confirmFinish() {
        AlertDialog.Builder(this)
            .setTitle("Finalizar Orden")
            .setMessage("¿Confirma que desea finalizar esta orden de trabajo?")
            .setPositiveButton("Finalizar") { _, _ ->
                ordenRepository.updateEstado(orderId, "FINALIZADA")
                Toast.makeText(this, "Orden finalizada correctamente", Toast.LENGTH_SHORT).show()
                loadOrderData()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
