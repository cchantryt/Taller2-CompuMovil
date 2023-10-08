package com.ch.taller2

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ch.taller2.databinding.ActivityMainBinding
import android.Manifest

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val PERM_LOCATION_CODE = 303
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Camara
        binding.botonCamara.setOnClickListener(){
            startActivity(Intent(this, CamaraActivity::class.java))
        }
        //Mapa
        binding.botonMapa.setOnClickListener(){
            //Solicitamos permisos de ubicacion
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //Toast.makeText(this, "mapa", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MapaActivity::class.java))
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    Toast.makeText(this, "El permiso de ubicacion es necesario", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERM_LOCATION_CODE)
                }
            }
        }
    }
}

/*PENDIENTES
*
* Camara: Completado
*
* Galeria:
* Permisos de galeria
*
* Mapa:
* Todo
*
* */