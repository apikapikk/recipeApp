package com.sebuahgrup.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ListRecipesActivity : AppCompatActivity() {
    private lateinit var homeButton : Button
    private lateinit var listButton : Button
    private lateinit var likedrecipesButton : Button
    private lateinit var actionRecipesButton : Button
    private lateinit var profileButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_recipes)

        //initialize button
        homeButton = findViewById(R.id.navigation_home_button)
        listButton = findViewById(R.id.navigation_list_recipes_button)
        likedrecipesButton = findViewById(R.id.navigation_liked_recipes_button)
        actionRecipesButton = findViewById(R.id.navigation_edit_recipes_button)
        profileButton = findViewById(R.id.navigation_account_button)

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
        likedrecipesButton.setOnClickListener {
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
    }
}