package com.example.kingsoftlog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import android.graphics.Color
import android.content.res.Configuration

class MainMenuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var switchModoOscuro: Switch
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var totalMateriales: TextView
    private lateinit var bajoStock: TextView
    private lateinit var pieChart: PieChart
    private lateinit var cardResumen: CardView

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemePreference()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Iniciamos vistas
        recyclerView = findViewById(R.id.recycler_actividades)
        switchModoOscuro = findViewById(R.id.switch_modo_oscuro)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        totalMateriales = findViewById(R.id.total_materiales)
        bajoStock = findViewById(R.id.bajo_stock)
        pieChart = findViewById(R.id.chart)
        cardResumen = findViewById(R.id.cardResumen)

        dbHelper = DBHelper(this)
        val isNightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        switchModoOscuro.isChecked = isNightMode

        // Configuramos el Switch para el Modo Oscuro
        switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            saveThemePreference(isChecked)
            // No llamamos a recreate() aquí
        }

        // Configuramos el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProductAdapter(emptyList())

        // Configurar el gráfico con datos reales
        configurarPieChart()

        // Agregamos listener al gráfico para filtrar por categoría
        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                if (e is PieEntry) {
                    val category = e.label
                    // Obtenemos productos de la categoría seleccionada
                    val products = getProductsForCategory(category)
                    recyclerView.adapter = ProductAdapter(products)
                }
            }
            override fun onNothingSelected() {
                recyclerView.adapter = ProductAdapter(emptyList())
            }
        })

        // Configurar la navegación inferior
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_view -> {
                    startActivity(Intent(this, ViewMaterialsActivity::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddMaterialsActivity::class.java))
                    true
                }
                R.id.nav_edit -> {
                    startActivity(Intent(this, EditMaterialsActivity::class.java))
                    true
                }
                R.id.nav_delete -> {
                    startActivity(Intent(this, DeleteMaterialsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        refreshSummary()
        actualizarColoresSegunTema()
    }

    override fun onResume() {
        super.onResume()
        refreshSummary()
        configurarPieChart()
        actualizarColoresSegunTema()
    }

    // Aplicamos la preferencia de tema guardada
    private fun applyThemePreference() {
        val sharedPref = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (isDarkMode && currentMode != AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (!isDarkMode && currentMode != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    // Función para guardar la preferencia del tema
    private fun saveThemePreference(isDarkMode: Boolean) {
        val sharedPref = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isDarkMode", isDarkMode)
            apply()
        }
    }

    private fun configurarPieChart() {
        val categoryCounts = dbHelper.getProductsCountByCategory()
        val orderedCategories = listOf("Repuestos", "Herramientas", "Accesorios", "Piezas")
        val entries = orderedCategories.map { category ->
            val actualCount = categoryCounts[category] ?: 0
            val valueForChart = if (actualCount == 0) 1f else actualCount.toFloat()
            PieEntry(valueForChart, category, actualCount)
        }
        val customColors = listOf(
            Color.parseColor("#FF5722"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#FFC107")
        )
        val dataSet = PieDataSet(entries, "Distribución de Materiales")
        dataSet.colors = customColors

        // ValueFormatter para mostrar el valor real (0 en lugar de 1)
        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return if (pieEntry?.data is Int) {
                    val realCount = pieEntry.data as Int
                    realCount.toString()
                } else {
                    super.getPieLabel(value, pieEntry)
                }
            }
        }

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.setMinAngleForSlices(5f)
        pieChart.setDrawEntryLabels(true)

        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            pieChart.setEntryLabelColor(Color.WHITE)
            pieChart.legend.textColor = Color.WHITE
            pieChart.description.textColor = Color.WHITE
            pieChart.setHoleColor(Color.TRANSPARENT)
            pieChart.setCenterTextColor(Color.WHITE)
        } else {
            pieChart.setEntryLabelColor(Color.BLACK)
            pieChart.legend.textColor = Color.BLACK
            pieChart.description.textColor = Color.BLACK
            pieChart.setHoleColor(Color.WHITE)
            pieChart.setCenterTextColor(Color.BLACK)
        }

        pieChart.description.isEnabled = false
        pieChart.legend.formSize = 15f
        pieChart.legend.xEntrySpace = 15f
        pieChart.centerText = "Materiales"
        pieChart.setCenterTextSize(14f)
        pieChart.invalidate()
    }

    private fun getProductsForCategory(category: String): List<Product> {
        return dbHelper.obtenerProductosPorCategoria(category)
    }

    // Adaptador para mostrar la lista de productos
    inner class ProductAdapter(private val products: List<Product>) :
        RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

        inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameText: TextView = itemView.findViewById(R.id.material_name)
            val stockText: TextView = itemView.findViewById(R.id.material_stock)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_material, parent, false)
            return ProductViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = products[position]
            holder.nameText.text = product.nombre
            holder.stockText.text = "Stock: ${product.stock}"
        }

        override fun getItemCount() = products.size
    }

    private fun refreshSummary() {
        // 1) Obtenemos el total de productos
        val total = dbHelper.getTotalProductsCount()
        totalMateriales.text = "Total de Materiales: $total"
        // 2) Obtenemos los productos con bajo stock (umbral 4)
        val lowStockList = dbHelper.getLowStockMaterials(4)
        // 3) Mostrar nombres en el TextView "bajoStock"
        if (lowStockList.isEmpty()) {
            bajoStock.text = "Materiales con bajo stock: (ninguno)"
        } else {
            val names = lowStockList.joinToString { it.nombre }
            bajoStock.text = "Materiales con bajo stock: $names"
        }
    }

    private fun actualizarColoresSegunTema() {
        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            totalMateriales.setTextColor(Color.WHITE)
            bajoStock.setTextColor(Color.RED) // Mantener rojo para bajo stock
            switchModoOscuro.setTextColor(Color.WHITE)
            cardResumen.setCardBackgroundColor(Color.DKGRAY)
        } else {
            totalMateriales.setTextColor(Color.BLACK)
            bajoStock.setTextColor(Color.RED) // Mantener rojo para bajo stock
            switchModoOscuro.setTextColor(Color.BLACK)
            cardResumen.setCardBackgroundColor(Color.WHITE)
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Aplicar el tema basado en la nueva configuración
        val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        // Actualizar colores y gráfico
        actualizarColoresSegunTema()
        configurarPieChart()
    }
}