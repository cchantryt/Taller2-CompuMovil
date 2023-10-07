package com.ch.taller2

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ch.taller2.databinding.ActivityMapaBinding
import com.google.android.gms.maps.SupportMapFragment


class MapaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el fragmento del mapa
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(binding.mapContainer.id, mapFragment)
            .commit()

        mapFragment.getMapAsync { googleMap ->
            // Configurar el mapa
        }
    }
}