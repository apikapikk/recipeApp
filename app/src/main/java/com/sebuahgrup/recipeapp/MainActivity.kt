package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.sebuahgrup.recipeapp.model.User


class MainActivity : AppCompatActivity() {

    //make variable to initialize element
    private lateinit var loginButton : Button
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var loginUsername : EditText
    private lateinit var loginPassword : EditText
    private lateinit var loadingProgress : FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //initialize element button, edittext, etc
        loginUsername = findViewById(R.id.login_username_text)
        loginPassword = findViewById(R.id.login_password_text)
        loginButton = findViewById(R.id.login_button_login)
        registerButton = findViewById(R.id.login_button_register)
        loadingProgress = findViewById(R.id.login_loading_progress_bar)
        auth = FirebaseAuth.getInstance()
        //action call homepage
        loginButton.setOnClickListener {
            //get text from edittext
            val usernameText = loginUsername.text.toString()
            val passwordText = loginPassword.text.toString()
            loginActionFunction(usernameText, passwordText)
        }
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
    //function to login user
    private fun loginActionFunction(usernameText: String, passwordText: String) {
        loadingProgress.visibility = View.VISIBLE
        if (usernameText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "Email atau Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
            auth.signInWithEmailAndPassword(usernameText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    loadingProgress.visibility = View.GONE
                    // Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show() Disable Toast
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Login Gagal : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                    loadingProgress.visibility = View.GONE
                Toast.makeText(this, "Gagal mengambil data pengguna: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}