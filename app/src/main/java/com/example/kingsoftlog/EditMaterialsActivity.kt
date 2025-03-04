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

class EditMaterialsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private lateinit var editTextNombre: EditText
    private lateinit var editTextDescripcion: EditText
    private lateinit var editTextPrecio: EditText
    private lateinit var editTextStock: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnTomarFoto: Button
    private lateinit var btnActualizar: Button
    private var selectedProductId: Int = -1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_materials)

        dbHelper = DBHelper(this)
        listView = findViewById(R.id.listViewEditMaterials)
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextDescripcion = findViewById(R.id.editTextDescripcion)
        editTextPrecio = findViewById(R.id.editTextPrecio)
        editTextStock = findViewById(R.id.editTextStock)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnActualizar = findViewById(R.id.buttonActualizar)

        cargarListaMateriales()
        configurarSpinner()

        listView.setOnItemClickListener { _, _, position, _ ->
            val producto = dbHelper.obtenerProductos()[position]
            selectedProductId = producto.id
            editTextNombre.setText(producto.nombre)
            editTextDescripcion.setText(producto.descripcion)
            editTextPrecio.setText(producto.precio.toString())
            editTextStock.setText(producto.stock.toString())

            val categoriaIndex = when (producto.categoryId) {
                1 -> 0
                2 -> 1
                3 -> 2
                4 -> 3
                else -> 0
            }
            spinnerCategoria.setSelection(categoriaIndex)
        }

        btnTomarFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 100)
        }

        btnActualizar.setOnClickListener {
            val nombre = editTextNombre.text.toString()
            val descripcion = editTextDescripcion.text.toString()
            val precio = editTextPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val stock = editTextStock.text.toString().toIntOrNull() ?: 0

            val categoriaSeleccionada = spinnerCategoria.selectedItem.toString()
            val categoryId = when (categoriaSeleccionada) {
                "Repuestos" -> 1
                "Piezas" -> 2
                "Accesorios" -> 3
                "Herramientas" -> 4
                else -> null
            }

            val imageUrl = imageUri?.toString()

            if (selectedProductId != -1 && nombre.isNotEmpty() && descripcion.isNotEmpty()) {
                dbHelper.actualizarProducto(selectedProductId, nombre, descripcion, precio, stock, categoryId, imageUrl)
                Toast.makeText(this, "Material actualizado", Toast.LENGTH_SHORT).show()
                cargarListaMateriales()
            } else {
                Toast.makeText(this, "Selecciona un material y completa los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarListaMateriales() {
        val productos = dbHelper.obtenerProductos()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, productos.map { it.nombre })
        listView.adapter = adapter
    }

    private fun configurarSpinner() {
        val categorias = arrayOf("Repuestos", "Piezas", "Accesorios", "Herramientas")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            imageUri = guardarImagen(bitmap)
            Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarImagen(bitmap: Bitmap): Uri {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "material_${System.currentTimeMillis()}.jpg")
        val outStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
        return Uri.fromFile(file)
    }
}
