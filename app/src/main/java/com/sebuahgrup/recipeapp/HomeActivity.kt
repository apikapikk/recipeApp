package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sebuahgrup.recipeapp.model.User

class HomeActivity : AppCompatActivity() {

    //make variable to initialize element
    private lateinit var listButton : Button
    private lateinit var homeButton: ImageButton
    private lateinit var likedButton : ImageButton
    private lateinit var listButtonNav : ImageButton
    private lateinit var actionButton : ImageButton
    private lateinit var profileButton : ImageButton
    private lateinit var userGreetings : TextView
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        //initialize element
        listButton = findViewById(R.id.home_see_all_recipes_button)
        homeButton = findViewById(R.id.home_navigation_home_button)
        likedButton = findViewById(R.id.profile_navigation_liked_recipes_button)
        listButtonNav = findViewById(R.id.profile_navigation_list_recipes_button)
        actionButton = findViewById(R.id.profile_navigation_edit_recipes_button)
        profileButton = findViewById(R.id.profile_navigation_account_button)
        userGreetings = findViewById(R.id.home_greetings_user_label)
        auth = FirebaseAuth.getInstance()
        //action call page
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        likedButton.setOnClickListener {
            val intent = Intent(this,LikedRecipesActivity::class.java)
            startActivity(intent)
        }
        listButton.setOnClickListener {
            val intent = Intent(this,ListRecipesActivity::class.java)
            startActivity(intent)
        }
        listButtonNav.setOnClickListener {
            val intent = Intent(this, ListRecipesActivity::class.java)
            startActivity(intent)
        }
        actionButton.setOnClickListener {
            val intent = Intent(this, ActionRecipesActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        //call function to display current user name in user greetings label
        displayUser()
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
            } else {
                Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
                }.addOnFailureListener {
                    Toast.makeText(this, "Gagal mengambil data pengguna: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
