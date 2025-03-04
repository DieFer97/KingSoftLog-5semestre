package com.example.kingsoftlog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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
                Toast.makeText(this@ViewMaterialsActivity, "Imagen de ${material.nombre}", Toast.LENGTH_SHORT).show()
                // Aquí puedes abrir un diálogo o una nueva pantalla para ver la imagen
            }
            return view
        }
    }
}
