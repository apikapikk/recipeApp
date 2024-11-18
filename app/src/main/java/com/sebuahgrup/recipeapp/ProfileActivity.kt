package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sebuahgrup.recipeapp.model.User

class ProfileActivity : AppCompatActivity() {
    private lateinit var homeButton : ImageButton
    private lateinit var listButton : ImageButton
    private lateinit var likedRecipesButton : ImageButton
    private lateinit var actionRecipesButton : ImageButton
    private lateinit var profileButton : ImageButton
    private lateinit var userGreetings : TextView
    private lateinit var userNameProfile : EditText
    private lateinit var usernameProfile : EditText
    private lateinit var passwordProfile : EditText
    private lateinit var actionButton: Button
    private lateinit var auth : FirebaseAuth
    private lateinit var loadingProgressBar: FrameLayout
    private var isBackToHome = false
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        //initialize button
        loadingProgressBar = findViewById(R.id.profile_loading_progress_bar)
        homeButton = findViewById(R.id.profile_navigation_home_button)
        listButton = findViewById(R.id.profile_navigation_list_recipes_button)
        likedRecipesButton = findViewById(R.id.profile_navigation_liked_recipes_button)
        actionRecipesButton = findViewById(R.id.profile_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.profile_navigation_account_button)
        userGreetings = findViewById(R.id.profile_greetings_user_label)
        userNameProfile = findViewById(R.id.profile_name_text)
        usernameProfile = findViewById(R.id.profile_username_text)
        passwordProfile = findViewById(R.id.profile_password_text)
        actionButton = findViewById(R.id.profile_button_action_edit)
        auth = FirebaseAuth.getInstance()

        //action call Homepage
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        //action call ListRecipesPage
        listButton.setOnClickListener {
            val intent = Intent(this, ListRecipesActivity::class.java)
            startActivity(intent)
        }
        //action call LikedRecipesPage
        likedRecipesButton.setOnClickListener {
            val intent = Intent(this, LikedRecipesActivity::class.java)
            startActivity(intent)
        }
        //action call ActionRecipesPage
        actionRecipesButton.setOnClickListener {
            val intent = Intent(this, ActionRecipesActivity::class.java)
            startActivity(intent)
        }
        //action call ProfilePage
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        actionButton.setOnClickListener {
            if (isEditing) {
                updateUserData()
            } else {
                enableEditing(true)
            }
        }
        displayUser()
        onBackPressedDispatcher.addCallback(this) {
            handlePress()
        }
    }
    private fun handlePress() {
        if (isBackToHome) {
            finishAffinity()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            isBackToHome = true
        }
    }
    //function to display current user name in user greetings label
    private fun displayUser() {
        val uid = auth.currentUser?.uid
        if (uid != null){
            getUserName(uid)
        }else{
            Toast.makeText(this, "User Belum Login", Toast.LENGTH_SHORT).show()
        }
    }
    //function to get current user login name
    private fun getUserName(uid: String) {
        val db = FirebaseDatabase.getInstance().getReference("users")
        db.child(uid).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                userGreetings.text = user.name
                userNameProfile.text = Editable.Factory.getInstance().newEditable(user.name)
                usernameProfile.text = Editable.Factory.getInstance().newEditable(user.email)
                passwordProfile.text = Editable.Factory.getInstance().newEditable(user.password)

            } else {
                Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data pengguna: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun enableEditing(enable: Boolean) {
        isEditing = enable
        userNameProfile.isEnabled = enable
        usernameProfile.isEnabled = enable
        passwordProfile.isEnabled = enable
        actionButton.text = if (enable) "Update" else "Edit"
    }
    private fun updateUserData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val db = FirebaseDatabase.getInstance().getReference("users").child(uid)
            val updatedUser = mapOf(
                "name" to userNameProfile.text.toString(),
                "email" to usernameProfile.text.toString(),
                "password" to passwordProfile.text.toString()
            )

            db.updateChildren(updatedUser).addOnSuccessListener {
                Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                enableEditing(false) // Kembali ke mode non-editable
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User belum login.", Toast.LENGTH_SHORT).show()
        }
    }
}