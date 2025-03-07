package com.example.kingsoftlog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var loginFormLayout: LinearLayout
    private lateinit var registerFormLayout: LinearLayout

    private lateinit var studentIdLoginEditText: EditText
    private lateinit var passwordLoginEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLinkTextView: TextView

    private lateinit var studentIdRegisterEditText: EditText
    private lateinit var emailRegisterEditText: EditText
    private lateinit var passwordRegisterEditText: EditText
    private lateinit var careerSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var loginLinkTextView: TextView

    private val careers = arrayOf("Ing. De Software", "Diseño Gráfico")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar la base de datos
        dbHelper = DBHelper(this)

        loginFormLayout = findViewById(R.id.loginFormLayout)
        registerFormLayout = findViewById(R.id.registerFormLayout)

        studentIdLoginEditText = findViewById(R.id.studentIdLoginEditText)
        passwordLoginEditText = findViewById(R.id.passwordLoginEditText)
        loginButton = findViewById(R.id.loginButton)
        registerLinkTextView = findViewById(R.id.registerLinkTextView)

        studentIdRegisterEditText = findViewById(R.id.studentIdRegisterEditText)
        emailRegisterEditText = findViewById(R.id.emailRegisterEditText)
        passwordRegisterEditText = findViewById(R.id.passwordRegisterEditText)
        careerSpinner = findViewById(R.id.careerSpinner)
        saveButton = findViewById(R.id.saveButton)
        loginLinkTextView = findViewById(R.id.loginLinkTextView)

        val logoImageView: ImageView = findViewById(R.id.loginLogoImageView)
        Glide.with(this)
            .load("https://api.a0.dev/assets/image?text=modern%20technology%20company%20logo%20king%20crown&aspect=1:1")
            .into(logoImageView)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, careers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        careerSpinner.adapter = adapter

        registerLinkTextView.setOnClickListener {
            showRegisterForm()
        }

        loginLinkTextView.setOnClickListener {
            showLoginForm()
        }

        loginButton.setOnClickListener {
            handleLogin()
        }

        saveButton.setOnClickListener {
            handleRegister()
        }
    }

    private fun showRegisterForm() {
        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 300
        loginFormLayout.startAnimation(fadeOut)
        loginFormLayout.visibility = View.GONE

        registerFormLayout.visibility = View.VISIBLE
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 300
        registerFormLayout.startAnimation(fadeIn)
    }

    private fun showLoginForm() {
        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 300
        registerFormLayout.startAnimation(fadeOut)
        registerFormLayout.visibility = View.GONE

        loginFormLayout.visibility = View.VISIBLE
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 300
        loginFormLayout.startAnimation(fadeIn)
    }

    private fun handleLogin() {
        val studentId = studentIdLoginEditText.text.toString().trim()
        val password = passwordLoginEditText.text.toString().trim()

        if (studentId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (dbHelper.checkUser(studentId, password)) {
            // Guardar el nombre del usuario en SharedPreferences
            val prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
            prefs.edit().putString("nombre_usuario", studentId).apply()

            val intent = Intent(this, MainMenuActivity::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "ID de alumno o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleRegister() {
        val studentId = studentIdRegisterEditText.text.toString().trim()
        val email = emailRegisterEditText.text.toString().trim()
        val password = passwordRegisterEditText.text.toString().trim()
        val career = careers[careerSpinner.selectedItemPosition]

        if (studentId.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (dbHelper.checkStudentIdExists(studentId)) {
            Toast.makeText(this, "Este ID de alumno ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        if (dbHelper.checkEmailExists(email)) {
            Toast.makeText(this, "Este correo electrónico ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        if (dbHelper.addUser(studentId, email, password, career)) {
            Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()

            // Guardar el nombre del usuario en SharedPreferences
            val prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
            prefs.edit().putString("nombre_usuario", studentId).apply()

            val intent = Intent(this, MainMenuActivity::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
        }
    }
}
