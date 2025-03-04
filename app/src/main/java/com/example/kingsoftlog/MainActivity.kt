package com.example.kingsoftlog

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var logoImageView: ImageView
    private lateinit var sloganTextView: TextView
    private lateinit var enterButton: Button
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar la base de datos
        dbHelper = DBHelper(this)

        // Inicializar vistas
        titleTextView = findViewById(R.id.titleTextView)
        logoImageView = findViewById(R.id.logoImageView)
        sloganTextView = findViewById(R.id.sloganTextView)
        enterButton = findViewById(R.id.enterButton)

        // Cargar logo desde URL usando Glide
        Glide.with(this)
            .load("https://api.a0.dev/assets/image?text=modern%20technology%20company%20logo%20king%20crown&aspect=1:1")
            .into(logoImageView)

        // Inicialmente ocultar todos los elementos
        titleTextView.visibility = View.INVISIBLE
        logoImageView.visibility = View.INVISIBLE
        sloganTextView.visibility = View.INVISIBLE
        enterButton.visibility = View.INVISIBLE

        // Animación para el título
        animateView(titleTextView, 0)

        // Animación para el logo
        animateView(logoImageView, 800)

        // Animación para el eslogan
        animateView(sloganTextView, 1800)

        // Animación para el botón
        animateView(enterButton, 2600)

        // Configurar el listener del botón
        enterButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun animateView(view: View, delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            view.visibility = View.VISIBLE
            val animation = AlphaAnimation(0.0f, 1.0f)
            animation.duration = 800
            view.startAnimation(animation)
        }, delay)
    }
}
