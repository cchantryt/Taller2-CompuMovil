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
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import java.io.IOException

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapaBinding

    //Mapa
    private lateinit var map: GoogleMap

    //Ubicacion usuario
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Seguimiento usuario
    private var rutaPolyline: Polyline? = null
    private val rutaCoordenadas: MutableList<LatLng> = mutableListOf()

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

        //Localizar al usuario
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Buscar direccion
        binding.searchButton.setOnClickListener {
            buscarDireccion()
        }
    }

    /*------------------------------------------------------------ FUNCIONES MAPA ------------------------------------------------------------*/
    //Mapa ready
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        updateMapStyle(luminosity = 0f)
        //Punto que ubica al usuario
        map.isMyLocationEnabled = true

        //Controles de zoom
        map.uiSettings.isZoomControlsEnabled = true

        //Posicion del usuario
        map.setOnMapClickListener { latLng ->
            //Agrega el punto al final de la ruta
            rutaCoordenadas.add(latLng)

            //Actualizar la polyline
            actualizarRutaPolyline()
        }

        //Evento LongClick en el mapa
        registerMapLongClickListener()

        // Listener para actualizar la ruta cuando la ubicación del usuario cambia
        map.setOnMyLocationChangeListener { location ->
            // Agrega la nueva ubicación a la ruta
            rutaCoordenadas.add(LatLng(location.latitude, location.longitude))

            // Actualiza la polyline
            actualizarRutaPolyline()
        }

        //Localizar al usuario
        //Verificamos permisos
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

    /*------------------------------------------------------------ FUNCIONES PARA BUSCAR DIRECCION ------------------------------------------------------------*/
    //Funcion para buscar una direccion
    private fun buscarDireccion() {
        val direccion = binding.editText.text.toString().trim()

        if (direccion.isNotEmpty()) {
            val geocoder = Geocoder(this)
            try {
                val addressList = geocoder.getFromLocationName(direccion, 1)
                if (addressList != null) {
                    if (addressList.isNotEmpty()) {
                        val address = addressList[0]
                        val latLng = LatLng(address.latitude, address.longitude)

                        //Borramos marcadores anteriores
                        map.clear()
                        // Crear un marcador en la dirección encontrada
                        map.addMarker(MarkerOptions().position(latLng).title(direccion))

                        //Movemos la camara
                        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        map.animateCamera(CameraUpdateFactory.zoomTo(15f))

                        //Agregar el punto al final de la ruta
                        rutaCoordenadas.add(latLng)
                        actualizarRutaPolyline()
                    } else {
                        Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                Log.e("MapaActivity", "Error al buscar la dirección: ${e.message}")
                Toast.makeText(this, "Error al buscar la dirección", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ingrese una dirección", Toast.LENGTH_SHORT).show()
        }
    }

    //Funcion para actualizar la ruta
    private fun actualizarRutaPolyline() {
        //Borramos la polyline anterior
        rutaPolyline?.remove()

        //Nueva polyline con las coordenadas de la ruta
        rutaPolyline = map.addPolyline(PolylineOptions()
            .addAll(rutaCoordenadas)
            .color(Color.BLUE)
            .width(5f)
        )
        //Nuevo punto en la ruta
        rutaPolyline?.points = rutaCoordenadas
    }

    //Evento LongClick en el mapa
    private fun registerMapLongClickListener() {
        map.setOnMapLongClickListener { latLng ->
            //Limpiamos marcadores
            map.clear()

            //Direccion del punto
            obtenerDireccionDesdeLatLng(latLng) { direccion ->
                //Nuevo marcador
                map.addMarker(MarkerOptions().position(latLng).title(direccion))

                //Movemos la camara al punto encontradoq
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                map.animateCamera(CameraUpdateFactory.zoomTo(15f))
            }
        }
    }

    //Funcion para obtener la direccion en LongClick
    private fun obtenerDireccionDesdeLatLng(latLng: LatLng, callback: (String) -> Unit) {
        val geocoder = Geocoder(this)
        try {
            val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                callback(address.getAddressLine(0) ?: "Dirección no disponible")
            } else {
                callback("Dirección no disponible")
            }
        } catch (e: IOException) {
            Log.e("MapaActivity", "Error: ${e.message}")
            callback("Dirección no disponible")
        }
    }

    /*------------------------------------------------------------ FUNCIONES SENSOR LUMINOSIDAD ------------------------------------------------------------*/
    //Sensor de luminosidad manager
    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    //Sensor de luminosidad oficial
    private val lightSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }
    //Registramos el listener del sensor de luminosidad
    private fun registerLightSensorListener() {
        lightSensor?.let {
            sensorManager.registerListener(lightSensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    //Borramos el listener del sensor de luminosidad
    private fun unregisterLightSensorListener() {
        sensorManager.unregisterListener(lightSensorListener)
    }
    //Registramos los datos del sensor de luminosidad cuando la app esta en primer plano
    override fun onResume() {
        super.onResume()
        registerLightSensorListener()
    }
    override fun onPause() {
        super.onPause()
        unregisterLightSensorListener()
    }

    //Listener para el sensor de luminosidad
    private val lightSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                //Obtenemos la luminosidad
                val luminosity = event.values[0]

                //Actualizamos el estilo del mapa
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
            //Claro
            R.raw.map_day
        } else {
            //Oscuro
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