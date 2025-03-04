package com.example.kingsoftlog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "kingsoft.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de categorías
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """)

        // Crear tabla de productos
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                price REAL,
                stock INTEGER DEFAULT 0,
                category_id INTEGER,
                image_url TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES categories (id)
            )
        """)

        // Crear tabla de usuarios
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                career TEXT NOT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """)

        // Insertar datos iniciales en categorías (seed data)
        db.execSQL("INSERT INTO categories (name) VALUES ('Repuestos')")
        db.execSQL("INSERT INTO categories (name) VALUES ('Piezas')")
        db.execSQL("INSERT INTO categories (name) VALUES ('Accesorios')")
        db.execSQL("INSERT INTO categories (name) VALUES ('Herramientas')")

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // ✅ Verificar usuario por studentId y contraseña
    fun checkUser(studentId: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM users WHERE student_id = ? AND password = ?", arrayOf(studentId, password))
        val exists = cursor.moveToFirst() && cursor.getInt(0) > 0
        cursor.close()
        return exists
    }

    // ✅ Verificar si un studentId ya existe
    fun checkStudentIdExists(studentId: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM users WHERE student_id = ?", arrayOf(studentId))
        val exists = cursor.moveToFirst() && cursor.getInt(0) > 0
        cursor.close()
        return exists
    }

    // ✅ Verificar si un email ya existe
    fun checkEmailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM users WHERE email = ?", arrayOf(email))
        val exists = cursor.moveToFirst() && cursor.getInt(0) > 0
        cursor.close()
        return exists
    }

    // ✅ Agregar usuario con fecha automática
    fun addUser(studentId: String, email: String, password: String, career: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("student_id", studentId)
            put("email", email)
            put("password", password)
            put("career", career)
        }
        val result = db.insert("users", null, values)
        return result != -1L
    }

    // ✅ Obtener todos los productos
    fun obtenerProductos(): List<Product> {
        val lista = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name, description, price, stock, category_id, image_url FROM products", null)
        while (cursor.moveToNext()) {
            val producto = Product(
                id = cursor.getInt(0),
                nombre = cursor.getString(1),
                descripcion = cursor.getString(2),
                precio = cursor.getDouble(3),
                stock = cursor.getInt(4),
                categoryId = cursor.getInt(5),
                imageUrl = cursor.getString(6)
            )
            lista.add(producto)
        }
        cursor.close()
        return lista
    }

    // ✅ Agregar un nuevo producto
    fun agregarProducto(nombre: String, descripcion: String, precio: Double, stock: Int, categoryId: Int?, imageUrl: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", nombre)
            put("description", descripcion)
            put("price", precio)
            put("stock", stock)
            put("category_id", categoryId)
            put("image_url", imageUrl)
        }
        db.insert("products", null, values)
        db.close()
    }

    // ✅ Eliminar un producto
    fun eliminarProducto(id: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete("products", "id=?", arrayOf(id.toString()))
        db.close()
        return rowsDeleted > 0
    }

    // ✅ Obtener un producto por su ID
    fun obtenerProductoPorId(id: Int): Product? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name, description, price, stock, category_id, image_url FROM products WHERE id = ?", arrayOf(id.toString()))
        val producto = if (cursor.moveToFirst()) {
            Product(
                id = cursor.getInt(0),
                nombre = cursor.getString(1),
                descripcion = cursor.getString(2),
                precio = cursor.getDouble(3),
                stock = cursor.getInt(4),
                categoryId = cursor.getInt(5),
                imageUrl = cursor.getString(6)
            )
        } else {
            null
        }
        cursor.close()
        return producto
    }

    // ✅ Actualizar producto
    fun actualizarProducto(id: Int, nombre: String, descripcion: String, precio: Double, stock: Int, categoryId: Int?, imageUrl: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", nombre)
            put("description", descripcion)
            put("price", precio)
            put("stock", stock)
            put("category_id", categoryId)
            put("image_url", imageUrl)
        }
        db.update("products", values, "id=?", arrayOf(id.toString()))
        db.close()
    }

    // Métodos nuevos para obtener datos reales desde la BD

    // 1) Obtener el total de productos
    fun getTotalProductsCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM products", null)
        val total = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return total
    }

    // 2) Obtener la cantidad de productos con bajo stock (umbral por defecto 5)
    fun getLowStockCount(threshold: Int = 5): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM products WHERE stock < ?", arrayOf(threshold.toString()))
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    // 3) Obtener el conteo de productos por categoría para construir el PieChart
    fun getProductsCountByCategory(): Map<String, Int> {
        val db = readableDatabase
        val map = mutableMapOf<String, Int>()
        val query = """
            SELECT c.name, COUNT(p.id) AS total
            FROM categories c
            LEFT JOIN products p ON p.category_id = c.id
            GROUP BY c.id
        """
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val categoryName = cursor.getString(0)
            val count = cursor.getInt(1)
            map[categoryName] = count
        }
        cursor.close()
        return map
    }

    // Obtener productos según la categoría
    fun obtenerProductosPorCategoria(categoryName: String): List<Product> {
        val lista = mutableListOf<Product>()
        val db = readableDatabase
        val query = """
            SELECT p.id, p.name, p.description, p.price, p.stock, p.category_id, p.image_url 
            FROM products p
            INNER JOIN categories c ON p.category_id = c.id
            WHERE c.name = ?
        """
        val cursor = db.rawQuery(query, arrayOf(categoryName))
        while (cursor.moveToNext()) {
            val producto = Product(
                id = cursor.getInt(0),
                nombre = cursor.getString(1),
                descripcion = cursor.getString(2),
                precio = cursor.getDouble(3),
                stock = cursor.getInt(4),
                categoryId = cursor.getInt(5),
                imageUrl = cursor.getString(6)
            )
            lista.add(producto)
        }
        cursor.close()
        return lista
    }

    fun getLowStockMaterials(threshold: Int = 4): List<Product> {
        val lista = mutableListOf<Product>()
        val db = readableDatabase
        // Consulta los productos con stock < threshold
        val cursor = db.rawQuery(
            "SELECT id, name, description, price, stock, category_id, image_url FROM products WHERE stock < ?",
            arrayOf(threshold.toString())
        )
        while (cursor.moveToNext()) {
            val producto = Product(
                id = cursor.getInt(0),
                nombre = cursor.getString(1),
                descripcion = cursor.getString(2),
                precio = cursor.getDouble(3),
                stock = cursor.getInt(4),
                categoryId = cursor.getInt(5),
                imageUrl = cursor.getString(6)
            )
            lista.add(producto)
        }
        cursor.close()
        return lista
    }
}
