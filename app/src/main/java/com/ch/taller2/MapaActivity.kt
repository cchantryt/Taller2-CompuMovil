package com.ch.taller2

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ch.taller2.databinding.ActivityMapaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.content.Context
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.android.gms.maps.model.MapStyleOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapaBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el fragmento del mapa
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /*------------------------------------------------------------ FUNCIONES MAPA ------------------------------------------------------------*/
    //Mapa ready
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        //Punto que ubica al usuario
        map.isMyLocationEnabled = true

        //Controles de zoom
        map.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        map.addMarker(MarkerOptions().position(userLocation).title("Mi Ubicación"))
                        map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
                        map.animateCamera(CameraUpdateFactory.zoomTo(15f))
                    }
                }
        }
    }
    //Registramos los datos del sensor de luminosidad
    override fun onResume() {
        super.onResume()
        registerLightSensorListener()
    }

    override fun onPause() {
        super.onPause()
        unregisterLightSensorListener()
    }

    /*------------------------------------------------------------ FUNCIONES SENSOR LUMINOSIDAD ------------------------------------------------------------*/
    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val lightSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    private fun registerLightSensorListener() {
        lightSensor?.let {
            sensorManager.registerListener(lightSensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterLightSensorListener() {
        sensorManager.unregisterListener(lightSensorListener)
    }

    //Listener para el sensor de luminosidad
    private val lightSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                // Obtén el valor de la luminosidad desde el sensor
                val luminosity = event.values[0]

                // Actualiza el estilo del mapa según la luminosidad
                updateMapStyle(luminosity)
            }
        }
    }

    //Umbral de luminosidad
    companion object {
        private const val LUMINOSITY_THRESHOLD = 700.0
    }

    //Funcion para actualizar el estilo del mapa
    private fun updateMapStyle(luminosity: Float) {
        val style = if (luminosity < LUMINOSITY_THRESHOLD) {
            // Estilo oscuro para baja luminosidad
            R.raw.map_day
        } else {
            // Estilo claro para alta luminosidad
            R.raw.map_night
        }

        try {
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, style))
            if (!success) {
                Log.e("MapaActivity", "Fallo al aplicar estilo de mapa.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapaActivity", "No se pudo encontrar el estilo de mapa. Error: $e")
        }
    }

}