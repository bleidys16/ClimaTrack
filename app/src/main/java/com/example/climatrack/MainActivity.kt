package com.example.climatrack

import android.os.Bundle
import com.example.climatrack.activities.BaseActivity

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupEdgeToEdge(findViewById(R.id.main))
    }
}