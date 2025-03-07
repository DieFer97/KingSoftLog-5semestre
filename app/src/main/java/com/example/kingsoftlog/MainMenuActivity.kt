package com.example.kingsoftlog

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater


class MainMenuActivity : AppCompatActivity() {

    // Referencias a las vistas del header
    private lateinit var btnHamburger: ImageButton
    private lateinit var textBienvenido: TextView
    private lateinit var textFechaHora: TextView
    private lateinit var companyIcon: ImageView

    // Referencias a las demás vistas
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

        // ============ HEADER ============
        btnHamburger = findViewById(R.id.btnHamburger)
        textBienvenido = findViewById(R.id.textBienvenido)
        textFechaHora = findViewById(R.id.textFechaHora)
        companyIcon = findViewById(R.id.companyIcon)

        // Configurar botón hamburguesa
        btnHamburger.setOnClickListener {
            val popup = androidx.appcompat.widget.PopupMenu(this, btnHamburger)
            popup.menuInflater.inflate(R.menu.menu_hamburger, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_cerrar_sesion -> {
                        cerrarSesion()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Mostrar nombre de usuario (ejemplo: guardado en SharedPreferences en el login)
        val prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val nombreUsuario = prefs.getString("nombre_usuario", "Invitado")
        textBienvenido.text = "Bienvenido, $nombreUsuario"

        // Mostrar fecha y hora actual
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy - HH:mm", Locale.getDefault())
        val fechaHora = dateFormat.format(Date())
        textFechaHora.text = fechaHora
        // ========== FIN HEADER ==========

        // Iniciamos vistas principales
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
            // No llamamos a recreate() aquí para evitar recargar la actividad de inmediato
        }

        // Configuramos el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProductAdapter(emptyList())

        // Configurar el gráfico con datos reales
        configurarPieChart()

        // Listener del gráfico para filtrar por categoría
        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                if (e is PieEntry) {
                    val category = e.label
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

    // Guardar la preferencia del tema
    private fun saveThemePreference(isDarkMode: Boolean) {
        val sharedPref = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isDarkMode", isDarkMode)
            apply()
        }
    }

    // Configurar la gráfica circular
    private fun configurarPieChart() {
        val categoryCounts = dbHelper.getProductsCountByCategory()
        // Ordenar las categorías en un orden específico
        val orderedCategories = listOf("Repuestos", "Herramientas", "Accesorios", "Piezas")
        val entries = orderedCategories.map { category ->
            val actualCount = categoryCounts[category] ?: 0
            // Si no hay productos, poner 1 para que se vea en la gráfica, pero el valor real es 0
            val valueForChart = if (actualCount == 0) 1f else actualCount.toFloat()
            PieEntry(valueForChart, category, actualCount)
        }

        val customColors = listOf(
            Color.parseColor("#FF5722"), // Naranja
            Color.parseColor("#4CAF50"), // Verde
            Color.parseColor("#2196F3"), // Azul
            Color.parseColor("#FFC107")  // Amarillo
        )

        val dataSet = PieDataSet(entries, "Distribución de Materiales")
        dataSet.colors = customColors

        // Mostrar el valor real (0) en lugar de 1
        dataSet.valueFormatter = object : ValueFormatter() {
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
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

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

    // Obtener productos por categoría
    private fun getProductsForCategory(category: String): List<Product> {
        return dbHelper.obtenerProductosPorCategoria(category)
    }

    // Adaptador para la lista de productos
    inner class ProductAdapter(private val products: List<Product>) :
        RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

        inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameText: TextView = itemView.findViewById(R.id.material_name)
            val stockText: TextView = itemView.findViewById(R.id.material_stock)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = layoutInflater.inflate(R.layout.item_material, parent, false)
            return ProductViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = products[position]
            holder.nameText.text = product.nombre
            holder.stockText.text = "Stock: ${product.stock}"
        }

        override fun getItemCount() = products.size
    }

    // Mostrar resumen de totales y bajo stock
    private fun refreshSummary() {
        val total = dbHelper.getTotalProductsCount()
        totalMateriales.text = "Total de Materiales: $total"

        val lowStockList = dbHelper.getLowStockMaterials(4)
        if (lowStockList.isEmpty()) {
            bajoStock.text = "Materiales con bajo stock: (ninguno)"
        } else {
            val names = lowStockList.joinToString { it.nombre }
            bajoStock.text = "Materiales con bajo stock: $names"
        }
    }

    // Ajustar colores según el modo actual
    private fun actualizarColoresSegunTema() {
        val isNightMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            totalMateriales.setTextColor(Color.WHITE)
            bajoStock.setTextColor(Color.RED)
            switchModoOscuro.setTextColor(Color.WHITE)
            cardResumen.setCardBackgroundColor(Color.DKGRAY)
        } else {
            totalMateriales.setTextColor(Color.BLACK)
            bajoStock.setTextColor(Color.RED)
            switchModoOscuro.setTextColor(Color.BLACK)
            cardResumen.setCardBackgroundColor(Color.WHITE)
        }
    }

    // Reaccionar a cambios de configuración (ej: modo oscuro activado/desactivado)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        actualizarColoresSegunTema()
        configurarPieChart()
    }

    // Lógica para cerrar sesión y volver al Login
    private fun cerrarSesion() {
        val prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
