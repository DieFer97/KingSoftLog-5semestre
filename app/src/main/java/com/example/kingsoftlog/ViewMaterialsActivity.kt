package com.example.kingsoftlog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import androidx.appcompat.app.AlertDialog



class ViewMaterialsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private lateinit var adapter: MaterialTableAdapter
    private var materiales = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_materials)

        dbHelper = DBHelper(this)
        listView = findViewById(R.id.listViewMaterials)
        cargarMateriales()
    }

    private fun cargarMateriales() {
        materiales = dbHelper.obtenerProductos().toMutableList()
        adapter = MaterialTableAdapter()
        listView.adapter = adapter
    }

    inner class MaterialTableAdapter : BaseAdapter() {
        override fun getCount(): Int = materiales.size
        override fun getItem(position: Int): Any = materiales[position]
        override fun getItemId(position: Int): Long = materiales[position].id.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = layoutInflater.inflate(R.layout.item_material_table, parent, false)
            val material = materiales[position]

            val nombreTextView = view.findViewById<TextView>(R.id.textMaterialName)
            val categoriaTextView = view.findViewById<TextView>(R.id.textMaterialCategory)
            val stockTextView = view.findViewById<TextView>(R.id.textMaterialStock)
            val btnVerImagen = view.findViewById<Button>(R.id.btnVerImagen)

            nombreTextView.text = material.nombre
            categoriaTextView.text = when (material.categoryId) {
                1 -> "Repuestos"
                2 -> "Piezas"
                3 -> "Accesorios"
                4 -> "Herramientas"
                else -> "Desconocido"
            }
            stockTextView.text = material.stock.toString()

            btnVerImagen.setOnClickListener {
                if (!material.imageUrl.isNullOrEmpty()) {
                    val builder = AlertDialog.Builder(this@ViewMaterialsActivity)
                    val dialogView = layoutInflater.inflate(R.layout.dialog_image, null)
                    val dialogImageView = dialogView.findViewById<ImageView>(R.id.dialogImageView)
                    dialogImageView.setImageURI(Uri.parse(material.imageUrl))
                    builder.setView(dialogView)
                        .setPositiveButton("Cerrar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                } else {
                    Toast.makeText(
                        this@ViewMaterialsActivity,
                        "No hay imagen disponible",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return view
        }
    }
}


