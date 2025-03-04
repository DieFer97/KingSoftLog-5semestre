package com.example.kingsoftlog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DeleteMaterialsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private lateinit var adapter: MaterialAdapter
    private var materiales = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_materials)

        dbHelper = DBHelper(this)
        listView = findViewById(R.id.listViewDeleteMaterials)
        cargarMateriales()
    }

    private fun cargarMateriales() {
        materiales = dbHelper.obtenerProductos().toMutableList()
        adapter = MaterialAdapter()
        listView.adapter = adapter
    }

    inner class MaterialAdapter : BaseAdapter() {
        override fun getCount(): Int = materiales.size
        override fun getItem(position: Int): Any = materiales[position]
        override fun getItemId(position: Int): Long = materiales[position].id.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = layoutInflater.inflate(R.layout.item_material_delete, parent, false)
            val material = materiales[position]

            val nombreTextView = view.findViewById<TextView>(R.id.textMaterialName)
            val btnEliminar = view.findViewById<Button>(R.id.btnEliminar)

            nombreTextView.text = material.nombre
            btnEliminar.setOnClickListener {
                dbHelper.eliminarProducto(material.id)
                Toast.makeText(this@DeleteMaterialsActivity, "Material eliminado", Toast.LENGTH_SHORT).show()
                cargarMateriales()
            }
            return view
        }
    }
}
