package com.ch.taller2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ch.taller2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botonCamara.setOnClickListener(){
            startActivity(Intent(this, CamaraActivity::class.java))
        }
        binding.botonMapa.setOnClickListener(){
            startActivity(Intent(this, MapaActivity::class.java))
        }

    }
}

/*PENDIENTES
*
* Camara: Completado
*
*
* Galeria:
* Permisos de galeria
*
*
* Mapa:
* Todo
*
* */