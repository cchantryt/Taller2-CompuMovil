package com.ch.taller2

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.content.FileProvider

import com.ch.taller2.databinding.ActivityCamaraBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.DateFormat
import java.util.Date


class CamaraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCamaraBinding

    /*Codes camara*/
    val PERM_CAMERA_CODE = 101
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_VIDEO_CAPTURE = 2

    /*Codes Galeria*/
    private var selectedUri: Uri? = null  // Variable para almacenar la URI seleccionada


    private lateinit var file: File

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

    //Funcion para visualizar el resultado de la foto o video y seleccion de galeria
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

        if (resultCode == Activity.RESULT_OK && data != null) {
            selectedUri = data.data

            // Mostrar la imagen o video en el ImageView o VideoView según el switch
            if (binding.switchFotoVideo.isChecked) {
                // Mostrar video
                binding.previewFoto.visibility = View.GONE
                binding.previewVideo.visibility = View.VISIBLE
                binding.previewVideo.setVideoURI(selectedUri)
                // Configurar el VideoView
                setupVideoView()
                binding.previewVideo.start()
            } else {
                // Mostrar imagen
                binding.previewFoto.visibility = View.VISIBLE
                binding.previewVideo.visibility = View.GONE
                binding.previewFoto.setImageURI(selectedUri)
            }
        }
    }

    //Funciones de guardado de fotos y videos
    // Ruta de almacenamiento
    private val storageDirectory: File by lazy {
        getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
    }

    // Guardar imagen
    private fun saveImageToLocation(bitmap: Bitmap?) {
        if (bitmap != null) {
            val imageFile = File(storageDirectory, "image_${System.currentTimeMillis()}.jpg")

            try {
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()

                // Agregar la imagen a la galería utilizando MediaStore
                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    imageFile.absolutePath,
                    imageFile.name,
                    "Descripción de la imagen"
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Guardar video
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

                // Agregar el video a la galería
                // En lugar de insertar directamente en MediaStore, copiamos el archivo a la carpeta de la galería
                val galleryFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                val galleryVideoFile = File(galleryFolder, videoFile.name)

                videoFile.copyTo(galleryVideoFile)

                // Notificar a la galería
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(galleryVideoFile)))

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    /*------------------------------------------------------------ FUNCIONES GALERIA ------------------------------------------------------------*/
    //Funcion para abrir la galeria
    private fun openGallery() {
        val intentPick = Intent(Intent.ACTION_PICK)
        intentPick.type = if (binding.switchFotoVideo.isChecked) "video/*" else "image/*"
        startActivityForResult(intentPick, 1)
    }

    //Funcion para configurar el VideoView
    private fun setupVideoView() {
        // Configurar un listener para detectar cuando el video termina
        binding.previewVideo.setOnCompletionListener {
            // Reiniciar la reproducción del video desde el principio
            binding.previewVideo.start()
        }
    }
}
