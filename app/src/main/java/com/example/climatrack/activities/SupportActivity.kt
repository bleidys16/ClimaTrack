package com.example.climatrack.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.example.climatrack.databinding.ActivitySupportBinding

class SupportActivity : BaseActivity() {

    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:018000CLIMA"))
            startActivity(intent)
        }

        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:soporte@climatrack.com"))
            startActivity(intent)
        }
    }
}
