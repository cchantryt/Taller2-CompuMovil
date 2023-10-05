package com.ch.taller2

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.ch.taller2.databinding.ActivityCamaraBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class CamaraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCamaraBinding

    val REQUEST_PICK = 3

    /*Permisos camara*/
    val PERM_CAMERA_CODE = 101
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_VIDEO_CAPTURE = 2
    var outputPath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamaraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*------------------------- CAMARA ------------------------------*/
        binding.botonTake.setOnClickListener() {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    takePhotoOrVideo()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    Toast.makeText(this, "El permiso de camara es necesario", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), PERM_CAMERA_CODE)
                }
            }
        }

        /*------------------------- GALERIA ------------------------------*/
        binding.botonGaleria.setOnClickListener() {
            openGallery()
            //Solicitar permisos de galeria
            /*No funciona la solicitud de galeria
            when{
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    //Si ya tiene permisos, se abre la galeria
                    openGallery()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    //Si no tiene permisos, se solicitan
                    Toast.makeText(this, "El permiso de galeria es necesario", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                }
            }*/
        }
    }

    /*------------------------------------------------------------ FUNCIONES CAMARA ------------------------------------------------------------*/
    //Funcion verificar si se toma foto o video
    private fun takePhotoOrVideo() {
        if (binding.switchFotoVideo.isChecked)
            //Video
            dispatchTakeVideoIntent()
        else
            //Foto
            dispatchTakePictureIntent()
    }

    //Funcion para tomar fotos
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    //Funcion para tomar videos
    private fun dispatchTakeVideoIntent() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        try {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se pudo tomar el video", Toast.LENGTH_SHORT).show()
        }
    }


    //Funcion para visualizar el resultado de la foto o video
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Si el resultado es de la foto
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Obtener la foto
            val imageBitmap = data?.extras?.get("data") as? Bitmap

            // Guardar la imagen en la ubicación deseada
            saveImageToLocation(imageBitmap)

            //Mostrar la foto
            //Ocultar el video
            binding.previewFoto.visibility = View.VISIBLE
            binding.previewVideo.visibility = View.GONE
            binding.previewFoto.setImageBitmap(imageBitmap)
        }

        //Si el resultado es del video
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            //Obtener el video
            val videoUri = data?.data

            // Guardar el video en la ubicación deseada
            saveVideoToLocation(videoUri)

            //Mostrar el video
            //Ocultar la foto
            binding.previewFoto.visibility = View.GONE
            binding.previewVideo.visibility = View.VISIBLE
            binding.previewVideo.setVideoURI(videoUri)
            binding.previewVideo.start()
        }
    }

    //Funciones para guardado de fotos y videos
    // Ruta de almacenamiento
    private val storageDirectory: File by lazy {
        getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
    }

    private fun saveImageToLocation(bitmap: Bitmap?) {
        if (bitmap != null) {
            val imageFile = File(storageDirectory, "image_${System.currentTimeMillis()}.jpg")

            try {
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun saveVideoToLocation(videoUri: Uri?) {
        videoUri?.let {
            val videoFile = File(storageDirectory, "video_${System.currentTimeMillis()}.mp4")

            try {
                val inputStream = contentResolver.openInputStream(videoUri)
                val outputStream = FileOutputStream(videoFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                Toast.makeText(this, "Video guardado", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /*------------------------------------------------------------ FUNCIONES GALERIA ------------------------------------------------------------*/
    private fun openGallery() {
        val intentPick = Intent(Intent.ACTION_PICK)
        intentPick.type =
            if(binding.switchFotoVideo.isChecked) "video/*" else "image/*"
        startActivityForResult(intentPick, 1)
    }



}
