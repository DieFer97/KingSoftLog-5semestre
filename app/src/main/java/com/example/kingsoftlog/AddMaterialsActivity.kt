package com.example.kingsoftlog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File


class AddMaterialsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnTomarFoto: Button

    //Variable para guardar la URI donde se almacenará la foto
    private var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_materials)

        val nombreEditText = findViewById<EditText>(R.id.editTextNombre)
        val descripcionEditText = findViewById<EditText>(R.id.editTextDescripcion)
        val precioEditText = findViewById<EditText>(R.id.editTextPrecio)
        val stockEditText = findViewById<EditText>(R.id.editTextStock)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        val btnGuardar = findViewById<Button>(R.id.buttonGuardar)

        dbHelper = DBHelper(this)

        //Configuramos el Spinner de Categorías
        val categorias = arrayOf("Repuestos", "Piezas", "Accesorios", "Herramientas")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter

        // Configuramos el botón para tomar la foto en alta resolución
        btnTomarFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            //Creamos el archivo donde se guardará la foto
            val photoFile = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "material_${System.currentTimeMillis()}.jpg"
            )

            //Obtenemos la URI usando FileProvider
            photoURI = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                photoFile
            )

            //Pasamos esa URI a la cámara para que guarde la imagen completa allí
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, 100)
        }

        // Guardamos en la BD
        btnGuardar.setOnClickListener {
            val nombre = nombreEditText.text.toString()
            val descripcion = descripcionEditText.text.toString()
            val precio = precioEditText.text.toString().toDoubleOrNull() ?: 0.0
            val stock = stockEditText.text.toString().toIntOrNull() ?: 0

            val categoriaSeleccionada = spinnerCategoria.selectedItem.toString()
            val categoryId = when (categoriaSeleccionada) {
                "Repuestos" -> 1
                "Piezas" -> 2
                "Accesorios" -> 3
                "Herramientas" -> 4
                else -> null
            }

            // Tomamos la URI final de la foto y la convertimos a String
            val imageUrl = photoURI?.toString()

            if (nombre.isNotEmpty() && descripcion.isNotEmpty()) {
                dbHelper.agregarProducto(nombre, descripcion, precio, stock, categoryId, imageUrl)
                Toast.makeText(this, "Material agregado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // **Recibimos el resultado de la cámara**
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            photoURI?.let {
                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
