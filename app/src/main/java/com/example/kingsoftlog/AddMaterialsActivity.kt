package com.example.kingsoftlog

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class AddMaterialsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnTomarFoto: Button
    private var imageUri: Uri? = null

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

        // **Configurar Spinner de Categorías**
        val categorias = arrayOf("Repuestos", "Piezas", "Accesorios", "Herramientas")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter

        // **Configurar botón de cámara**
        btnTomarFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 100)
        }

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

            val imageUrl = imageUri?.toString()  // Convertir `Uri` en String para guardar

            if (nombre.isNotEmpty() && descripcion.isNotEmpty()) {
                dbHelper.agregarProducto(nombre, descripcion, precio, stock, categoryId, imageUrl)
                Toast.makeText(this, "Material agregado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // **Manejar el resultado de la cámara**
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            imageUri = guardarImagen(bitmap)
            Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
        }
    }

    // **Guardar imagen en almacenamiento interno**
    private fun guardarImagen(bitmap: Bitmap): Uri {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "material_${System.currentTimeMillis()}.jpg")
        val outStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
        return Uri.fromFile(file)
    }
}
