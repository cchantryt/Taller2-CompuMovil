package com.ch.taller2

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ch.taller2.databinding.ActivityMapaBinding

class MapaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}