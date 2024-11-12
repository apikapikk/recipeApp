package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sebuahgrup.recipeapp.model.User

class RegisterActivity : AppCompatActivity() {

    //make a variable to initialize element
    private lateinit var  auth: FirebaseAuth
    private lateinit var buttonRegister : Button
    private lateinit var emailText : EditText
    private lateinit var passwordText : EditText
    private lateinit var nameText : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        //initialize element
        auth = FirebaseAuth.getInstance()
        buttonRegister = findViewById(R.id.register_button_register)
        emailText = findViewById(R.id.register_email_text)
        passwordText = findViewById(R.id.register_password_text)
        nameText = findViewById(R.id.register_name_text)

        //action button register
        buttonRegister.setOnClickListener {

            //create a variable to save input
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()

            //check empty or not
            if (email.isEmpty()){
                emailText.error = "value harus di isikan"
                emailText.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailText.error = "value harus di isikan"
                emailText.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()){
                passwordText.error = "value harus di isikan"
                passwordText.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6){
                passwordText.error = "value harus di isikan"
                passwordText.requestFocus()
                return@setOnClickListener
            }
            if (name.isEmpty()){
                nameText.error = "value harus di isikan"
                nameText.requestFocus()
                return@setOnClickListener
            }
            //call function register
            registerFirebase(email, password, name)
        }
    }
    //add user to realtime db
    private fun saveNewUserToDatabase(uid : String, email: String, password: String, name: String) {
        val database = FirebaseDatabase.getInstance().getReference("users")
        val user = User(uid, email, password, name)
        database.child(uid).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Data pengguna tersimpan", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Gagal menyimpan data pengguna: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

    }
    // register user function
    private fun registerFirebase(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    saveNewUserToDatabase(uid ,email, password, name)
                    Toast.makeText(this,"register successful",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,"register failed",Toast.LENGTH_SHORT).show()
                }
            }
    }
}